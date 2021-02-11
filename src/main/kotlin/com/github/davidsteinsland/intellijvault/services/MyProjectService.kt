package com.github.davidsteinsland.intellijvault.services

import com.github.davidsteinsland.intellijvault.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
