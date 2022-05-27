package com.github.mirokolodii.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SomeTask : DefaultTask() {

    @TaskAction
    fun doWork() {
        println("SomeTask doWork")
    }
}