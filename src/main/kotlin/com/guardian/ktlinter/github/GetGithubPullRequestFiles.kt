package com.guardian.ktlinter.github

import com.guardian.ktlinter.FetchedFile
import com.guardian.ktlinter.Value
import com.guardian.ktlinter.callExecutor
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.models.GithubPullRequest
import com.guardian.ktlinter.github.models.GithubPullRequestFile

class GetGithubPullRequestFiles(
    private val gitHubService: GitHubService,
    private val parseGitPatchIntoLines: ParseGitPatchIntoLines
) {

    operator fun invoke(githubPullRequest: GithubPullRequest): Value {
        return when (val pullRequestFiles = callExecutor(gitHubService.getPullRequestFiles(githubPullRequest.number))) {
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
