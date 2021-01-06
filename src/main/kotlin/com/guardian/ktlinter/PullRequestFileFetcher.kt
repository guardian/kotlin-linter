package com.guardian.ktlinter

import com.guardian.ktlinter.github.DownloadRelevantPullRequestFiles
import com.guardian.ktlinter.github.GetGithubPullRequestFiles
import com.guardian.ktlinter.github.GetPullRequestDetails
import com.guardian.ktlinter.github.models.GithubPullRequest
import java.io.File

interface PullRequestFileFetcher {

    fun fetch(id: Int): Value

}

data class PullRequest(
    val id: Int,
    val head: Commit,
    val branch: Branch,
    val files: List<DownloadedFile>
)

data class Branch(
    val name: String
)

data class Commit(
    val sha: String
)

data class FilenameAndDirectory(
    val directory: String,
    val fileName: String,
    val fullPath: String
)

data class DownloadedFile(
    val filenameAndDirectory: FilenameAndDirectory,
    val fetchedFile: FetchedFile,
    val file: File
)


data class FetchedFile(
    val filename: String,
    val rawFileUrl: String,
    val patches: List<Patch>
)

data class Patch(
    val fileName: String,
    val commitId: String,
    val lines: List<PatchLine>
) {
    val additions: List<PatchLine.Addition>
        get() = lines.filterIsInstance(PatchLine.Addition::class.java)
}

sealed class PatchLine {
    object NoChange : PatchLine()
    object Removal : PatchLine()
    object Metadata : PatchLine()
    data class Addition(
        val lineInFile: Int,
        val lineInPatch: Int,
        val change: String
    ) : PatchLine()
}

class GithubPullRequestFileFetcher(
    private val getPullRequestDetails: GetPullRequestDetails,
    private val getGithubPullRequestFiles: GetGithubPullRequestFiles,
    private val downloadRelevantPullRequestFiles: DownloadRelevantPullRequestFiles
) : PullRequestFileFetcher {

    private val fileSaveLocation: String = "tmp"

    override fun fetch(id: Int): Value {
        return when (val prValue = getPullRequestDetails(id)) {
            is Value.Data<*> -> {
                val githubPullRequest = prValue.data as GithubPullRequest
                when (val downloadedFiles = getGithubPullRequestFiles(githubPullRequest)) {
                    is Value.Data<*> -> {
                        val files = downloadedFiles.data as List<FetchedFile>
                        val pullRequestFileStore = "$fileSaveLocation/${githubPullRequest.number}/"
                        val relvantFiles =
                            downloadRelevantPullRequestFiles(githubPullRequest.head.ref, pullRequestFileStore, files)
                        Value.Data(
                            PullRequest(
                                githubPullRequest.number,
                                head = Commit(githubPullRequest.head.sha),
                                branch = Branch(githubPullRequest.head.ref),
                                files = relvantFiles
                            )
                        )
                    }
                    else -> downloadedFiles
                }
            }
            is Value.Error -> prValue
        }
    }
}