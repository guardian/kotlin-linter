package com.guardian.ktlinter.github

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
        pullRequestFiles: List<PullRequestFile>
    ) {
        pullRequestFiles.forEach { file ->
            val splitDirectories = file.filename.split("/")
            val directories = splitDirectories.toMutableList().let {
                it.removeAt(splitDirectories.lastIndex)
                it.joinToString("/")
            }
            val saveDirectory = "$filesStore$directories/"
            val fileName = saveDirectory + splitDirectories.last()

            try {
                Files.createDirectories(Paths.get(saveDirectory))
            } catch (exception: FileAlreadyExistsException) {
                println(exception)
            }
            val localFile = File(fileName)
            if (!localFile.exists()) {
                val contentCall = gitHubService.getFileContents(file.filename, ref)
                val contentsRequest = contentCall.execute()
                if (contentsRequest.isSuccessful) {
                    localFile.writeText(contentsRequest.body()!!)
                } else {
                    println("Response code: ${contentsRequest.code()}")
                }

            } else {
                println("$fileName already exists.")
            }
        }
    }
}