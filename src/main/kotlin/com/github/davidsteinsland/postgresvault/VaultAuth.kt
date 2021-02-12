package com.github.davidsteinsland.postgresvault

import com.fasterxml.jackson.core.JsonProcessingException
import com.intellij.application.ApplicationThreadPool
import com.intellij.credentialStore.Credentials
import com.intellij.database.dataSource.DatabaseAuthProvider
import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.url.JdbcUrlParserUtil
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import java.io.IOException
import java.util.concurrent.CompletionStage

class VaultAuth : DatabaseAuthProvider, CoroutineScope {
    private companion object {
        private fun determineCluster(host: String?) =
            host?.contains("prod")?.takeIf { it }?.let { "prod-fss" } ?: "preprod-fss"
    }

    private val vault = Vault()

    override val coroutineContext = SupervisorJob() + Dispatchers.ApplicationThreadPool + CoroutineName("VaultAuth")
    override fun getId() = "vault"

    override fun getDisplayName() = "Vault"

    override fun isApplicable(dataSource: LocalDataSource) =
        dataSource.dbms.isPostgres

    override fun intercept(connection: ProtoConnection, silent: Boolean): CompletionStage<ProtoConnection>? {
        val url = connection.connectionPoint.url

        val parser = JdbcUrlParserUtil.parsed(
            connection.connectionPoint,
            url
        )!!

        val database = parser.getParameter("database")
        val host = parser.getParameter("host")
        val cluster = determineCluster(host)

        if (database == null || host == null) {
            throw VaultAuthException("Failed to parse/extract host or database from the JDBC url")
        }

        return future {
            val json = try {
                vault.readJson("postgresql/$cluster/creds/$database-readonly")
            } catch (err: JsonProcessingException) {
                throw VaultAuthException("Failed to fetch credentials from Vault (cluster: $cluster)", err)
            } catch (err: IOException) {
                throw VaultAuthException("Failed to run vault command: ${err.message}", err)
            }

            val username = json.path("data").path("username").asText()
            val password = json.path("data").path("password").asText()

            if (username.isEmpty() || password.isEmpty()) {
                throw VaultAuthException("Failed to parse username and password from Vault response")
            }

            DatabaseCredentialsAuthProvider.applyCredentials(
                connection,
                Credentials(username, password),
                true
            )
        }
    }

    internal class VaultAuthException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
}
