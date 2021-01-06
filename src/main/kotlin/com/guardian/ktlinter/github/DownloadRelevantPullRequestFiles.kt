package com.guardian.ktlinter.github

import com.guardian.ktlinter.*
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths

class DownloadRelevantPullRequestFiles(
    private val gitHubService: GitHubService
) {

    operator fun invoke(
        ref: String,
        filesStore: String,
        githubPullRequestFiles: List<FetchedFile>
    ): List<DownloadedFile> {
        return githubPullRequestFiles.mapNotNull { fetchedFile ->
            when (val value = callExecutor(gitHubService.getFileContents(fetchedFile.filename, ref))) {
                is Value.Data<*> -> {
                    val filenameAndDirectory = splitDirectoriesAndFileName(filesStore, fetchedFile)
                    val saveDirectory = "$filesStore${filenameAndDirectory.directory}/"
                    try {
                        Files.createDirectories(Paths.get(saveDirectory))
                    } catch (exception: FileAlreadyExistsException) {
                        println(exception)
                    }
                    val file = File(filenameAndDirectory.fileName).apply {
                        writeText(value.data as String)
                    }
                    DownloadedFile(
                        filenameAndDirectory,
                        fetchedFile,
                        file
                    )
                }
                is Value.Error -> null
            }
        }
    }

    private fun splitDirectoriesAndFileName(
        saveDirectory: String,
        fetchedFile: FetchedFile
    ): FilenameAndDirectory {
        val splitDirectories = fetchedFile.filename.split("/").toMutableList()
        val fileName = splitDirectories.removeAt(splitDirectories.lastIndex)
        val directory = saveDirectory + splitDirectories.joinToString("/")
        return FilenameAndDirectory(
            directory = directory,
            fileName = fileName,
            fullPath = directory + fileName
        )
    }


}