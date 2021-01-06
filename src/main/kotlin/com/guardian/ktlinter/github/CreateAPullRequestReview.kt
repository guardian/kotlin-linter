package com.guardian.ktlinter.github

import com.guardian.ktlinter.PullRequest
import com.guardian.ktlinter.Value
import com.guardian.ktlinter.callExecutor

class CreateAPullRequestReview(
    private val gitHubService: GitHubService
) {
    operator fun invoke(
        pullRequest: PullRequest,
        review: PostAReview
    ): Value {
        return callExecutor(gitHubService.postAReview(pullRequest.id, review))
    }
}