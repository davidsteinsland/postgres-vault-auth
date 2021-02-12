package com.github.davidsteinsland.postgresvault.services

import com.github.davidsteinsland.postgresvault.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
