package com.github.mirokolodii

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveRequest
import com.google.api.services.drive.DriveScopes
import org.apache.http.client.utils.URIBuilder
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.io.File
import com.google.api.services.drive.model.File as DriveFile

class GoogleClient(
    private val clientId: String,
    private val clientSecret: String,
    private val secretsDataStoreFactory: FileDataStoreFactory,
    private val logger: Logger
) {

    companion object {
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()
        private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        private val SCOPES = listOf(DriveScopes.DRIVE)

        private const val APPLICATION_NAME = "com.github.mirokolodii.AndroidFilesUploaderPlugin"

        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
        private const val DEFAULT_SPACES = "drive"
        private const val DEFAULT_CORPORA = "user"
        private const val DEFAULT_FILE_CONTENT_TYPE = "application/octet-stream"
    }

    private val service = Drive.Builder(
        HTTP_TRANSPORT,
        JSON_FACTORY,
        getCredentials()
    )
        .setApplicationName(APPLICATION_NAME)
        .build()


    private fun getCredentials(): Credential {
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            clientId,
            clientSecret,
            SCOPES
        )
            .setDataStoreFactory(secretsDataStoreFactory)
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()

        //returns an authorized Credential object.
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Creates missing folders in path, starting with *parent*, and returns last folder's id
     */
    fun createFoldersInPath(parent: String, folders: List<String>): String {
        return folders.fold(parent) { currentParent, currentName ->
            val foldersResult = service.files().list()
                .setSpaces(DEFAULT_SPACES)
                .setCorpora(DEFAULT_CORPORA)
                .setQ(
                    "name = '$currentName'" +
                            " and '$currentParent' in parents" +
                            " and not trashed" +
                            " and mimeType = '$FOLDER_MIME_TYPE'"
                )
                .execute()
                .files

            val folder = if (foldersResult.isNotEmpty()) {
                foldersResult[0]
            } else {
                val toCreate = DriveFile()
                    .setName(currentName)
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setParents(listOf(currentParent))
                service.files().create(toCreate).execute()
            }

            folder.id
        }
    }

    private fun findInFolder(destinationParentId: String, name: String): List<DriveFile> {
        val query = "'$destinationParentId' in parents and not trashed and name = '$name'"
        return service.files().list()
            .setSpaces(DEFAULT_SPACES)
            .setCorpora(DEFAULT_CORPORA)
            .setFields("files(id, name)")
            .setQ(query)
            .execute()
            .files
    }

    fun uploadFile(destinationParentId: String, file: File, replaceIfExists: Boolean) {

        val content = FileContent(DEFAULT_FILE_CONTENT_TYPE, file)

        val existingDestinationFiles = findInFolder(destinationParentId, file.name).sortedBy { it.modifiedTime.value }

        val modificationRequest: DriveRequest<DriveFile> = if (existingDestinationFiles.isNotEmpty()) {
            if (replaceIfExists) {
                val updatedFile = existingDestinationFiles.first()
                logger.lifecycle("File with name '${file.name}' already exists, replacing ...")
                service.files().update(updatedFile.id, null, content)
            } else {
                throw GradleException(
                    "File already exists in GoogleDrive.\n" +
                            "Name: ${file.name}, id: ${existingDestinationFiles.first().id}"
                )

            }
        } else {
            logger.lifecycle("Uploading file '${file.name}'")
            val driveFile = DriveFile().apply {
                name = file.name
                parents = listOf(destinationParentId)
            }
            service.files().create(driveFile, content)
        }

        modificationRequest.setProgressLogger(logger)

        val createdDriveFile = modificationRequest.execute()

        // TODO set permissions
        /* DriveFile updated = modificationRequest . execute ()

         logger.debug('Creating permissions...')
         BatchRequest permissionsBatchRequest = googleClient . drive . batch ()
         permissions.each {
             googleClient.drive.permissions().create(updated.getId(), it)
                 .queue(
                     permissionsBatchRequest, new SimpleJsonBatchCallBack (
                             'Could not update permissions')
                 )
         }
         permissionsBatchRequest.execute()*/

        logger.lifecycle("File uploaded: ${file.canonicalPath}\nas ${createdDriveFile.getLink()}")
    }
}

private fun DriveRequest<DriveFile>.setProgressLogger(logger: Logger) {
    mediaHttpUploader.setProgressListener { uploader ->
        logger.lifecycle("Uploaded: ${uploader.uploadState} ${uploader.numBytesUploaded}[bytes](${uploader.progress * 100})")
    }
}

private fun DriveFile.getLink() = URIBuilder("https://drive.google.com/open")
    .addParameter("id", id)
    .build()