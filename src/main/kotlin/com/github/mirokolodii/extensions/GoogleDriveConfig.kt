package com.github.mirokolodii.extensions

import org.gradle.api.provider.Property

abstract class GoogleDriveConfig {
    abstract val googleDriveClientId: Property<String>
    abstract val googleDriveSecret: Property<String>
    abstract val googleDriveCredentialsDirPath: Property<String>
    abstract val replaceFileIfExists: Property<Boolean>
    abstract val destinationFolderId: Property<String>
    abstract val destinationFolderPath: Property<String>
}