package com.github.davidsteinsland.intellijvault

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

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
        executeAndReturnExitCode("vault", "login", "-method=oidc")
    }

    private fun isAuthenticated() =
        executeAndReturnExitCode("vault", "token", "lookup") == 0

    private fun executeAndReturnExitCode(vararg command: String) =
        execute(ProcessBuilder(*command).inheritIO(), Process::waitFor)

    private fun executeAndReturnJson(vararg command: String) =
        execute(ProcessBuilder(*command)) {
            mapper.readValue(it.inputStream, ObjectNode::class.java)
        }

    private fun <R> execute(pb: ProcessBuilder, block: (Process) -> R) =
        block(pb.start())
}
