package com.github.mirokolodii

import com.github.mirokolodii.tasks.google_drive.UploadFilesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFilesUploaderPlugin : Plugin<Project> {

    companion object {
        private const val EXTENSION_NAME = "filesUploaderConfig"
        private const val DEFAULT_TASK_NAME = "uploadFiles"
    }

    override fun apply(project: Project) {

        val config = project.extensions.create(EXTENSION_NAME, ConfigExtension::class.java)

        val i = project.tasks.register(DEFAULT_TASK_NAME, UploadFilesTask::class.java) { task ->
            task.googleDriveClientId.set(config.googleDriveClientId)
            task.googleDriveSecret.set(config.googleDriveSecret)
            task.googleDriveCredentialsDirPath.set(config.googleDriveCredentialsDirPath)

            task.sourceFolderPath.set(config.sourceFolderPath)

            task.destinationFolderId.set(config.destinationFolderId)
            task.destinationFolderPath.set(config.destinationFolderPath)

            task.doFirst {
                task.logger.error("doFirst")
            }

            task.doLast {
                task.logger.error("doLast")
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