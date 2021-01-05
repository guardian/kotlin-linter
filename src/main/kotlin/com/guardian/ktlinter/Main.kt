package com.guardian.ktlinter


fun main() {
    val pullRequestReviewer = PullRequestReviewer.create(
        PullRequestReviewer.Config("tmp/", "report/")
    )
    pullRequestReviewer.review(6445)
}


