package com.guardian.ktlinter


fun main() {
    val pullRequestReviewer = PullRequestReviewer.create(
        PullRequestReviewer.Config("tmp/", "reports/")
    )
    pullRequestReviewer.review(6445)
}


