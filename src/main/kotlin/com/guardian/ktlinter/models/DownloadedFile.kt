package com.guardian.ktlinter.models

import java.io.File

data class DownloadedFile(
    val filenameAndDirectory: FilenameAndDirectory,
    val fetchedFile: FetchedFile,
    val file: File
)