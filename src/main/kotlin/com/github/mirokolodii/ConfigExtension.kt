package com.github.mirokolodii

import org.gradle.api.provider.Property

abstract class ConfigExtension {
    abstract val googleDriveClientId: Property<String>
    abstract val googleDriveSecret: Property<String>
    abstract val googleDriveCredentialsDirPath: Property<String>

    abstract val sourceFolderPath: Property<String>

    abstract val destinationFolderId: Property<String>
    abstract val destinationFolderPath: Property<String>
}