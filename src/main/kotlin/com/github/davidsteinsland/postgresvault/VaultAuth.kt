package com.github.davidsteinsland.postgresvault

import com.fasterxml.jackson.core.JsonProcessingException
import com.intellij.application.ApplicationThreadPool
import com.intellij.credentialStore.Credentials
import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.dataSource.DatabaseAuthProvider
import com.intellij.database.dataSource.DatabaseConnectionInterceptor.ProtoConnection
import com.intellij.database.dataSource.DatabaseCredentialsAuthProvider
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.url.JdbcUrlParserUtil
import com.intellij.database.dataSource.url.template.MutableParametersHolder
import com.intellij.database.dataSource.url.template.ParametersHolder
import com.intellij.database.dataSource.url.ui.UrlPropertiesPanel.createLabelConstraints
import com.intellij.database.dataSource.url.ui.UrlPropertiesPanel.createSimpleConstraints
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridLayoutManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletionStage
import javax.swing.JPanel

class VaultAuth : DatabaseAuthProvider, CoroutineScope {
    private val vault = Vault()

    override val coroutineContext = SupervisorJob() + Dispatchers.ApplicationThreadPool + CoroutineName("VaultAuth")
    override fun getId() = "vault"

    override fun getDisplayName() = VaultBundle.message("name")

    override fun isApplicable(dataSource: LocalDataSource) =
        dataSource.dbms.isPostgres

    override fun createWidget(
        project: Project?,
        credentials: DatabaseCredentials,
        dataSource: LocalDataSource
    ): DatabaseAuthProvider.AuthWidget {
        return VaultWidget(dataSource)
    }

    override fun intercept(connection: ProtoConnection, silent: Boolean): CompletionStage<ProtoConnection>? {
        val mountPath = connection.connectionPoint.additionalJdbcProperties["vault.path"]
            ?: throw VaultAuthException(VaultBundle.message("invalidMountPath"))

        return future {
            val json = try {
                vault.readJson(mountPath)
            } catch (err: JsonProcessingException) {
                throw VaultAuthException(VaultBundle.message("jsonError"), err)
            }

            val username = json.path("data").path("username").asText()
            val password = json.path("data").path("password").asText()

            if (username.isEmpty() || password.isEmpty()) {
                throw VaultAuthException(VaultBundle.message("invalidResponse"))
            }

            DatabaseCredentialsAuthProvider.applyCredentials(
                connection,
                Credentials(username, password),
                true
            )
        }
    }

    @Suppress("TooManyFunctions", "EmptyFunctionBlock", "MagicNumber")
    private class VaultWidget(dataSource: LocalDataSource) : DatabaseAuthProvider.AuthWidget {
        private val pathField = JBTextField()
        private val panel = JPanel(GridLayoutManager(1, 6)).apply {
            val pathLabel = JBLabel(VaultBundle.message("pathLabel"))
            add(pathLabel, createLabelConstraints(0, 0, pathLabel.preferredSize.getWidth()))
            add(pathField, createSimpleConstraints(0, 1, 3))

            // dataSource
            val parser = JdbcUrlParserUtil.parsed(
                dataSource.connectionConfig,
                dataSource.url
            ) ?: return@apply

            val host = parser.getParameter("host") ?: return@apply
            val db = parser.getParameter("database") ?: return@apply
            determineMountPath(host, db)
        }

        override fun save(dataSource: LocalDataSource, copyCredentials: Boolean) {
            dataSource.additionalJdbcProperties["vault.path"] = pathField.text
        }

        override fun reset(dataSource: LocalDataSource, copyCredentials: Boolean) {
            pathField.text = (dataSource.additionalJdbcProperties["vault.path"] ?: "")
        }

        override fun updateFromUrl(holder: ParametersHolder) {
            val host = holder.getParameter("host") ?: return
            val database = holder.getParameter("database") ?: return
            determineMountPath(host, database)
        }

        override fun updateUrl(holder: MutableParametersHolder) {}

        private fun determineMountPath(host: String, database: String) {
            val cluster = host.contains("prod").takeIf { it }?.let { "prod-fss" } ?: "preprod-fss"
            val role = pathField.text?.split('/')?.lastOrNull()?.takeIf { it.startsWith(database) } ?: "$database-readonly"
            pathField.text = "postgresql/$cluster/creds/$role"
        }

        override fun isPasswordChanged(): Boolean {
            return false
        }

        override fun hidePassword() {
        }

        override fun reloadCredentials() {
        }

        override fun getComponent() = panel

        override fun getPreferredFocusedComponent() = pathField

        override fun forceSave() {
        }
    }

    internal class VaultAuthException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
}
