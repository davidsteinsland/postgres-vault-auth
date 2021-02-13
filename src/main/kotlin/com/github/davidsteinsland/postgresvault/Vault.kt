package com.github.davidsteinsland.postgresvault

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException

internal class Vault {
    private companion object {
        private val mapper = jacksonObjectMapper()
    }

    fun readJson(path: String): ObjectNode {
        authenticate()
        return executeAndReturnJson("vault", "read", "-format=json", path)
    }

    private fun authenticate() {
        if (isAuthenticated()) return
        executeAndReturnJson("vault", "login", "-method=oidc", "-format=json")
    }

    private fun isAuthenticated(): Boolean =
        execute("vault", "token", "lookup") {
            val errorText = it.errorStream.bufferedReader().readText()
            if (it.exitValue() != 0 && !errorText.contains("permission denied")) {
                throw IOException(VaultBundle.message("authenticationFailed", errorText))
            }
            it.exitValue() == 0
        }

    private fun <R> execute(vararg command: String, onSuccess: (Process) -> R) =
        execute(ProcessBuilder(*command), onSuccess)

    private fun executeAndReturnJson(vararg command: String) =
        execute(ProcessBuilder(*command)) {
            if (it.exitValue() != 0) {
                val errorText = it.errorStream.bufferedReader().readText()
                throw IOException(VaultBundle.message("processFailed", errorText))
            }
            mapper.readValue(it.inputStream, ObjectNode::class.java)
        }

    private fun <R> execute(pb: ProcessBuilder, onSuccess: (Process) -> R) =
        pb.start().also { it.waitFor() }.let(onSuccess)
}
