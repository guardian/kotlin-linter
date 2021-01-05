package com.guardian.ktlinter.github

import com.guardian.ktlinter.Value
import com.guardian.ktlinter.callExecutor

class GetPullRequestFiles(
    private val gitHubService: GitHubService
) {

    operator fun invoke(pullRequest: PullRequest): Value {
        return callExecutor(gitHubService.getPullRequestFiles(pullRequest.number))
    }
}