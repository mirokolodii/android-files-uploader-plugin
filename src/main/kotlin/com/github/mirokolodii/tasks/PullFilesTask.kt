package com.github.mirokolodii.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PullFilesTask : DefaultTask() {

    companion object {
        private const val SHOULD_PULL_FILES_DEFAULT_VALUE = true
    }

    @get:Input
    @get:Optional
    abstract val shouldPullFilesFromDevice: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val adbExePath: Property<String>

    @get:Input
    @get:Optional
    abstract val deviceFolderPath: Property<String>

    @get:Input
    @get:Optional
    abstract val localFolderPath: Property<String>

    @TaskAction
    fun doWork() {
        if (!shouldPullFilesFromDevice.getOrElse(SHOULD_PULL_FILES_DEFAULT_VALUE)) {
            logger.lifecycle("Pulling files from device is disabled. You can enable with flag 'shouldPullFilesFromDevice'.")
            return
        }

        createDeviceFolder()
        createLocalFolder()
        pullFiles()
        cleanupDeviceFolder()
    }

    private fun createDeviceFolder() {
        project.exec {
            it.executable = adbExePath.get()
            it.args = listOf("shell", "mkdir", "-p", deviceFolderPath.get())
        }
    }

    private fun createLocalFolder() {
        File(localFolderPath.get()).mkdirs()
    }

    private fun pullFiles() {
        logger.lifecycle("Pulling files from device...")
        project.exec {
            it.executable = adbExePath.get()
            it.args = listOf("pull", deviceFolderPath.get() + "/.", localFolderPath.get())
        }
    }

    private fun cleanupDeviceFolder() {
        logger.lifecycle("Cleaning-up folder '${deviceFolderPath.get()}' on device...")
        project.exec {
            it.executable = adbExePath.get()
            it.args = listOf("shell", "rm", "-r", deviceFolderPath.get())
        }
    }

}