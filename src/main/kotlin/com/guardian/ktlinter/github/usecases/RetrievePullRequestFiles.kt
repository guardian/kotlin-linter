package com.guardian.ktlinter.github.usecases

import com.guardian.ktlinter.models.Value
import com.guardian.ktlinter.executeCall
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.models.GithubPullRequest
import com.guardian.ktlinter.github.models.GithubPullRequestFile
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.models.FetchedFile

class RetrievePullRequestFiles(
    private val gitHubService: GitHubService,
    private val parseGitPatchIntoLines: ParseGitPatchIntoLines
) {

    operator fun invoke(githubPullRequest: GithubPullRequest): Value {
        return when (val pullRequestFiles = executeCall(gitHubService.getPullRequestFiles(githubPullRequest.number))) {
            is Value.Data<*> -> {
                val files = pullRequestFiles.data as List<GithubPullRequestFile>
                Value.Data(files.map { githubPullRequestFile ->
                    FetchedFile(
                        filename = githubPullRequestFile.filename,
                        rawFileUrl = githubPullRequestFile.raw_url,
                        patches = githubPullRequestFile.patch.split("@@ -")
                            .filterNot { s -> s.isEmpty() }
                            .map {
                                parseGitPatchIntoLines.invoke(
                                    githubPullRequestFile.filename,
                                    githubPullRequestFile.sha,
                                    it
                                )
                            }
                    )
                }
                )

            }
            else -> pullRequestFiles
        }
    }
}
