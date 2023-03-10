package com.github.mirokolodii.tasks

import com.github.mirokolodii.GoogleClient
import com.google.api.client.util.store.FileDataStoreFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UploadFilesTask : DefaultTask() {

    companion object {
        private const val SHOULD_UPLOAD_FILES_DEFAULT_VALUE = true
        private const val REPLACE_FILES_IF_EXIST_DEFAULT_VALUE = true
    }

    @get:Input
    @get:Optional
    abstract val shouldUploadFiles: Property<Boolean>

    @get:Input
    abstract val googleDriveClientId: Property<String>

    @get:Input
    abstract val googleDriveSecret: Property<String>

    @get:Input
    abstract val googleDriveCredentialsDirPath: Property<String>

    @get:InputDirectory
    abstract val sourceFolderPath: Property<String>

    @get:Input
    @get:Optional
    abstract val destinationFolderId: Property<String>

    @get:Input
    @get:Optional
    abstract val destinationFolderPath: Property<String>

    @get:Input
    @get:Optional
    abstract val replaceFileIfExists: Property<Boolean>

    @TaskAction
    fun doWork() {

        if (!shouldUploadFiles.getOrElse(SHOULD_UPLOAD_FILES_DEFAULT_VALUE)) {
            logger.lifecycle("Uploading files is disabled. You can enable with flag 'shouldUploadFiles'.")
            return
        }

        //TODO handle properly credentials data store
        val f = File(googleDriveCredentialsDirPath.get())
        val googleClient = GoogleClient(
            googleDriveClientId.get(),
            googleDriveSecret.get(),
            //TODO alternatively create as in Google example
            FileDataStoreFactory(File(googleDriveCredentialsDirPath.get())),
            logger
        )

        val rootDestinationFolderId = identifyRootDestinationFolderId(googleClient)

        val fileTree = project.fileTree(sourceFolderPath)
        fileTree.files.forEach { file ->
            val subPath = file.parent.removePrefix(fileTree.dir.canonicalPath)

            val destinationParentId = googleClient.createFoldersInPath(
                rootDestinationFolderId,
                subPath.splitToNestedFolders()
            )

            googleClient.uploadFile(
                destinationParentId,
                file,
                replaceFileIfExists.getOrElse(REPLACE_FILES_IF_EXIST_DEFAULT_VALUE)
            )
        }
    }

    private fun identifyRootDestinationFolderId(googleClient: GoogleClient): String {
        if (destinationFolderId.isPresent && !destinationFolderPath.isPresent) {
            return destinationFolderId.get()
        }
        if (!destinationFolderId.isPresent && destinationFolderPath.isPresent) {
            return googleClient.createFoldersInPath(
                "root",
                destinationFolderPath.get().splitToNestedFolders()
            )
        }
        throw GradleException("You must specify either destinationFolderId or destinationFolderPath")
    }
}

private fun String.splitToNestedFolders() = split(File.separator).filter { it.isNotBlank() }