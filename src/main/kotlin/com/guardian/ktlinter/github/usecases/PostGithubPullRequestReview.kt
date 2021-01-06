package com.guardian.ktlinter.github.usecases

import com.guardian.ktlinter.Value
import com.guardian.ktlinter.executeCall
import com.guardian.ktlinter.github.models.GithubReview
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.models.PullRequest

class PostGithubPullRequestReview(
    private val gitHubService: GitHubService
) {
    operator fun invoke(
        pullRequest: PullRequest,
        review: GithubReview
    ): Value {
        return executeCall(gitHubService.postAReview(pullRequest.id, review))
    }
}