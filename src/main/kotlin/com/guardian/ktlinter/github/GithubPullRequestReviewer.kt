package com.guardian.ktlinter.github

import com.guardian.ktlinter.executeCall
import com.guardian.ktlinter.github.models.GithubComment
import com.guardian.ktlinter.github.models.GithubReview
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.models.Value
import com.guardian.ktlinter.reports.models.Report
import com.guardian.ktlinter.reports.models.SuggestedChange
import com.guardian.ktlinter.reviews.PullRequestReviewer

internal class GithubPullRequestReviewer(
    private val gitHubService: GitHubService
) : PullRequestReviewer {
    override fun review(pullRequest: PullRequest, report: Report) {

        val review = if (report.suggestedChanges.isEmpty()) {
            GithubReview(
                "Ktlint has not suggested any changes. 👍",
                "COMMENT",
                emptyList()
            )
        } else {
            GithubReview(
                "Hello, checkout the comments from ktlint.",
                "COMMENT",
                report.suggestedChanges.map { it.toReviewComment() }
            )
        }
        when (val reviewValue = executeCall(gitHubService.postAReview(pullRequest.id, review))) {
            is Value.Data<*> -> println("Review is successfully submitted.")
            is Value.Error -> println(reviewValue.message)
        }
    }


    private fun SuggestedChange.toReviewComment(): GithubComment {
        return GithubComment(suggestion, file, line)
    }
}