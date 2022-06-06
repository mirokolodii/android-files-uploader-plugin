# Android Files Uploader Plugin
This Gradle plugin allows to pull files from Android device via ADB to local folder and then upload them (including folders structure) to Google Drive.

Inspired by https://github.com/valnaumov/gradle-google-drive-plugin.

## Prerequisites
You need to have Google Drive API credentials created first.
1. Go to [Google API Console](https://console.developers.google.com/flows/enableapi?apiid=drive "Google API Console") and create a project (if not created yet).
2. Go to [Credentials section](https://console.developers.google.com/apis/credentials "Credentials section") of this project.
3. Choose Create credentials → OAuth client id → Web application. Copy `id` and  `secret`.

## Installation
Follow instruction in [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.mirokolodii.android-files-uploader "Gradle Plugin Portal").

## Configuration
Add extension named `filesUploaderConfig` as in example below.
```js
// Configuration of Gradle plugin 'android-files-uploader'
filesUploaderConfig {

    // Controls, whether or not files should be pulled from Android device to local folder.
    // Optional (default value is 'true').
    shouldPullFilesFromDevice = true

    // Full path to ADB.
    // In Android projects, use Android gradle plugin as below:
    adbExePath = android.getAdbExe().toString()

    // Path to folder in Android device, which will be used to pull files from.
    deviceFolderPath = '/sdcard/Pictures/screenshots'

    /* Local folder path.
    This folder is used:
	- as destination for files, which will be pulled from the device,
    - as source for files, that should uploaded to Google Drive.
    All missing folders will be created.
	*/
    localFolderPath = "$rootDir/screenshots/"

    // Controls, whether or not files should be uploaded from local folder to Google Drive.
    // Optional (default value is 'true').
	// If set to 'true', you need to specify `googleDrive` section as well.
    shouldUploadFiles = true

    // GoogleDrive configuration
    googleDrive {
        // Do not put client id and secret directly into the build file.
        googleDriveClientId = "<client id>"
        googleDriveSecret = "<secret>"

        // Location where Google Drive client's credentials will be stored.
        googleDriveCredentialsDirPath = "path/to/credentials"

        // Destination folder in GoogleDrive
        // All missing folders will be created.
        destinationFolderPath = "some/path/"
		// or use folder id
        //    destinationFolderId = "<some id>"
    }
}
```

##Usage
There are 3 tasks in this plugin:
1. `pullFiles` - pulls files from Android device to local folder
2. `uploadFiles` - uploads files from local folder to Google Drive folder
3. `pullAndUploadFiles` - combination of above two.

## Example usage
Can be used in combination with Android instrumented tests, which are making screenshots, to pull those screenshots from a device and upload to Google Drive.

Below configuration in `build.gradle` allows to pull and upload screenshots right after `connectedDebugAndroidTest ` task is finished.
```js
tasks.whenTaskAdded { task ->
    if (task.name == 'connectedDebugAndroidTest') {
        task.finalizedBy("pullAndUploadFiles")
    }
}
```
