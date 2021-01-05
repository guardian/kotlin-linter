package com.guardian.ktlinter.usecases

import com.guardian.ktlinter.GitHubService
import com.guardian.ktlinter.PostAReview
import com.guardian.ktlinter.ReviewComment

class CreateAPullRequestReview(
    private val gitHubService: GitHubService
) {
    operator fun invoke(
        pullRequestId: String,
        commitId: String,
        event: String = "COMMENT",
        comments: List<ReviewComment>
    ): Boolean {
        val createAReview = PostAReview(
            "Hello, checkout the comments from ktlint.",
            event,
            comments = comments
        )
        val postACommentCall = gitHubService.postAReview(pullRequestId, createAReview)
        val postACommentRequest = postACommentCall.execute()
        return if (postACommentRequest.isSuccessful) {
//            println("Comment successfully posted to pull request with ID $pullRequestId.")
            true
        } else {
            println("There was an error whilst posting a comment to a pull request with the ID: $pullRequestId.")
            println("HTTP code: " + postACommentRequest.code())
            println("HTTP Response Body: " + postACommentRequest.errorBody()?.string())
            false
        }
    }
}