package com.github.mirokolodii

import com.github.mirokolodii.tasks.SomeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidScreenshotsUploaderPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val i = project.tasks.register("someTask", SomeTask::class.java) {
            it.doFirst {
                println("someTask doFirst")
            }
            it.doLast {
                println("someTask doLast")
            }
        }


        project.task("sampleTask") { it ->
            it.doFirst {
                println("sampleTask do first ${it.didWork}")
            }
            it.doLast {
                println("sampleTask Do last ${it.didWork}")
            }

//            it.dependsOn("someTask")
            it.dependsOn(i)
        }
    }
}