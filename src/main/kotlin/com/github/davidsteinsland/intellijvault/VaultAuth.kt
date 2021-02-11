package com.github.davidsteinsland.intellijvault

import com.intellij.application.ApplicationThreadPool
import com.intellij.credentialStore.Credentials
import com.intellij.database.dataSource.DatabaseAuthProvider
import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.url.JdbcUrlParserUtil
import com.intellij.database.dataSource.url.StatelessJdbcUrlParser
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.net.URI
import java.util.concurrent.CompletionStage

class VaultAuth : DatabaseAuthProvider, CoroutineScope {
    private companion object {
        private val clusters = mapOf(
            "prod-pg.intern.nav.no" to "prod-fss",
            "dev-pg.intern.nav.no" to "preprod-fss"
        )

        private fun determineCluster(host: String?) =
            host?.let { clusters[host] } ?: "preprod-fss"
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

        return future {
            val json = vault.readJson("postgresql/$cluster/creds/$database-readonly")
            DatabaseCredentialsAuthProvider.applyCredentials(connection, Credentials(json.path("data").path("username").asText(), json.path("data").path("password").asText()), true)
        }
    }
}