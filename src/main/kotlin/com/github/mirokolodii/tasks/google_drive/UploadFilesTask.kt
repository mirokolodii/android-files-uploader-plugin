package com.github.mirokolodii.tasks.google_drive

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
        logger.error("SomeTask doWork")

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

        val i = project.fileTree(sourceFolderPath)
        println(i.dir.canonicalPath)
        i.files.forEach { file ->
            println("----------")
            println("files in source: ${file.name}")
            println("files in source: ${file.path}")
            val subPath = file.parent.removePrefix(i.dir.canonicalPath)
            println("files in source: $subPath")
            val destinationParentId = googleClient.createFoldersInPath(
                rootDestinationFolderId,
                subPath.splitToNestedFolders()
            )

            googleClient.uploadFile(destinationParentId, file, replaceFileIfExists.getOrElse(true))
        }

        // TODO debug
//        googleClient.listFolders()
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

private fun String.splitToNestedFolders() = split("/").filter { it.isNotBlank() }