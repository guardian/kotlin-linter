package com.guardian.ktlinter.github

import com.guardian.ktlinter.pullrequests.PullRequestFileFetcher
import com.guardian.ktlinter.models.Value
import com.guardian.ktlinter.executeCall
import com.guardian.ktlinter.github.models.GithubPullRequest
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.github.usecases.DownloadRelevantPullRequestFiles
import com.guardian.ktlinter.github.usecases.RetrievePullRequestFiles
import com.guardian.ktlinter.models.FetchedFile
import com.guardian.ktlinter.models.PullRequest

internal class GithubPullRequestFileFetcher(
    private val gitHubService: GitHubService,
    private val retrievePullRequestFiles: RetrievePullRequestFiles,
    private val downloadRelevantPullRequestFiles: DownloadRelevantPullRequestFiles
) : PullRequestFileFetcher {

    private val fileSaveLocation: String = "tmp"

    override fun fetch(id: Int): Value {
        return when (val prValue = executeCall(gitHubService.getPullRequestDetails(id))) {
            is Value.Data<*> -> {
                val githubPullRequest = prValue.data as GithubPullRequest
                when (val downloadedFiles = retrievePullRequestFiles(githubPullRequest)) {
                    is Value.Data<*> -> {
                        val files = downloadedFiles.data as List<FetchedFile>
                        val pullRequestFileStore = "$fileSaveLocation/${githubPullRequest.number}/"
                        val relevantFiles =
                            downloadRelevantPullRequestFiles(githubPullRequest.head.ref, pullRequestFileStore, files)
                        Value.Data(
                            PullRequest(
                                githubPullRequest.number,
                                head = PullRequest.Commit(githubPullRequest.head.sha),
                                branch = PullRequest.Branch(githubPullRequest.head.ref),
                                files = relevantFiles,
                                localFiles = pullRequestFileStore
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