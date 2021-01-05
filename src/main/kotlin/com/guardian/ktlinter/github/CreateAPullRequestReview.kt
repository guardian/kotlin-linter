package com.guardian.ktlinter.github

class CreateAPullRequestReview(
    private val gitHubService: GitHubService
) {
    operator fun invoke(
        pullRequestId: String,
        suggestedChanges: List<SuggestedChange>
    ): Boolean {
        val createAReview = PostAReview(
            "Hello, checkout the comments from ktlint.",
            "COMMENT",
            comments = suggestedChanges.map { it.toReviewComment() }
        )
        val postACommentCall = gitHubService.postAReview(pullRequestId, createAReview)
        val postACommentRequest = postACommentCall.execute()
        return if (postACommentRequest.isSuccessful) {
            true
        } else {
            println("There was an error whilst posting a comment to a pull request with the ID: $pullRequestId.")
            println("HTTP code: " + postACommentRequest.code())
            println("HTTP Response Body: " + postACommentRequest.errorBody()?.string())
            false
        }
    }

    private fun SuggestedChange.toReviewComment(): ReviewComment {
        val comment = ktLintError.message
        return ReviewComment(
            comment,
            file,
            line
        )
    }
}