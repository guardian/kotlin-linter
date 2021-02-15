package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.data.ChangeRequest
import com.theguardian.ktlinter.changerequests.data.ChangedFile
import com.theguardian.ktlinter.changerequests.github.GitHubRepositoryService
import com.theguardian.ktlinter.changerequests.github.ParseGitPatchIntoLines
import com.theguardian.ktlinter.changerequests.github.data.GithubPullRequestFile

/**
 * An implementation of [RetrieveChangeRequest] that uses the Github Rest API to retrieve a pull requests details and
 * associated files.
 *
 * @param gitHubRepositoryService a retrofit service responsible for handling pull request related information
 * @param parseGitPatchIntoLines an interactor responsible for parsing a patch
 */
internal class GithubRetrieveChangeRequest(
    private val gitHubRepositoryService: GitHubRepositoryService,
    private val parseGitPatchIntoLines: ParseGitPatchIntoLines
) : RetrieveChangeRequest {

    override suspend fun retrieve(changeRequestId: String): ChangeRequest {
        val pullRequestDetails = gitHubRepositoryService.getPullRequestDetails(changeRequestId.toInt())
        val pullRequestFiles = gitHubRepositoryService.getPullRequestFiles(changeRequestId.toInt())
        return ChangeRequest(
            pullRequestDetails.number,
            head = ChangeRequest.Commit(pullRequestDetails.head.sha),
            branch = ChangeRequest.Branch(pullRequestDetails.head.ref),
            changedFiles = pullRequestFiles.map(::githubFileToChangedFile),
            owner = pullRequestDetails.user.login
        )
    }

    private fun githubFileToChangedFile(file: GithubPullRequestFile): ChangedFile {
        return ChangedFile(
            filename = file.filename,
            rawFileUrl = file.raw_url,
            patches = file.patch.split("@@ -")
                .filterNot { s -> s.isEmpty() }
                .map { line ->
                    parseGitPatchIntoLines(file.filename, file.sha, line)
                }
        )
    }
}