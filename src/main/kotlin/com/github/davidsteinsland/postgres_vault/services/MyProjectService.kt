package com.github.davidsteinsland.postgres_vault.services

import com.github.davidsteinsland.postgres_vault.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
