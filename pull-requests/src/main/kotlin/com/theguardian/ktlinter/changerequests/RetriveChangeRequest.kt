package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.data.ChangeRequest
import com.theguardian.ktlinter.changerequests.data.ChangedFile
import com.theguardian.remoterepository.RemoteRepository

/**
 * An implementation of [RetrieveChangeRequest] that uses the Github Rest API to retrieve a pull requests details and
 * associated files.
 *
 * @param githubRemoteRepository a remote repositoryy
 * @param parseGitPatchIntoLines an interactor responsible for parsing a patch
 */
internal class RetriveChangeRequest(
    private val githubRemoteRepository: RemoteRepository,
    private val parseGitPatchIntoLines: ParseGitPatchIntoLines
) : RetrieveChangeRequest {

    override suspend fun retrieve(changeRequestId: String): ChangeRequest {
        val pullRequestDetails = githubRemoteRepository.getChangeRequestDetails(changeRequestId.toInt())
        val pullRequestFiles = githubRemoteRepository.getChangeRequestFiles(changeRequestId.toInt())
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