package com.github.mirokolodii.extensions

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class ConfigExtension {
    abstract val adbExePath: Property<String>
    abstract val deviceFolderPath: Property<String>
    abstract val localFolderPath: Property<String>
    abstract val shouldPullFilesFromDevice: Property<Boolean>
    abstract val shouldUploadFiles: Property<Boolean>

    @get:Nested
    abstract val googleDrive: GoogleDriveConfig

    fun googleDrive(action: Action<in GoogleDriveConfig?>) {
        action.execute(googleDrive)
    }
}