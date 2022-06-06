package com.github.mirokolodii

import com.github.mirokolodii.extensions.ConfigExtension
import com.github.mirokolodii.tasks.PullFilesTask
import com.github.mirokolodii.tasks.UploadFilesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFilesUploaderPlugin : Plugin<Project> {

    companion object {
        private const val EXTENSION_NAME = "filesUploaderConfig"
        private const val PULL_AND_UPLOAD_TASK_NAME = "pullAndUploadFiles"
        private const val PULL_TASK_NAME = "pullFiles"
        private const val UPLOAD_TASK_NAME = "uploadFiles"
    }

    override fun apply(project: Project) {

        val config = project.extensions.create(EXTENSION_NAME, ConfigExtension::class.java)

        project.task(PULL_AND_UPLOAD_TASK_NAME) { task ->
            task.dependsOn(PULL_TASK_NAME)
            task.finalizedBy(UPLOAD_TASK_NAME)
        }

        project.tasks.register(PULL_TASK_NAME, PullFilesTask::class.java) { task ->
            task.shouldPullFilesFromDevice.set(config.shouldPullFilesFromDevice)
            task.adbExePath.set(config.adbExePath)
            task.deviceFolderPath.set(config.deviceFolderPath)
            task.localFolderPath.set(config.localFolderPath)
        }

        project.tasks.register(UPLOAD_TASK_NAME, UploadFilesTask::class.java) { task ->
            task.shouldUploadFiles.set(config.shouldUploadFiles)
            task.replaceFileIfExists.set(config.googleDrive.replaceFileIfExists)

            task.googleDriveClientId.set(config.googleDrive.googleDriveClientId)
            task.googleDriveSecret.set(config.googleDrive.googleDriveSecret)
            task.googleDriveCredentialsDirPath.set(config.googleDrive.googleDriveCredentialsDirPath)

            task.sourceFolderPath.set(config.localFolderPath)

            task.destinationFolderId.set(config.googleDrive.destinationFolderId)
            task.destinationFolderPath.set(config.googleDrive.destinationFolderPath)
        }
    }
}