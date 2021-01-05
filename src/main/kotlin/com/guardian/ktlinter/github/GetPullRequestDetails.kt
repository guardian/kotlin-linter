package com.guardian.ktlinter.github

import com.guardian.ktlinter.Value
import com.guardian.ktlinter.callExecutor

class GetPullRequestDetails(
    private val gitHubService: GitHubService
) {

    operator fun invoke(pullRequestNumber: Int): Value {
        return callExecutor(gitHubService.getPullRequestDetails(pullRequestNumber))
    }
}