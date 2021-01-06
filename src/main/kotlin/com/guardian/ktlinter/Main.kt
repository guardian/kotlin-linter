package com.guardian.ktlinter


fun main(args: Array<String>) {
    assert(args.size > 1) { "This program requires a parameter to run." }
    val pullRequestId = args[0].toInt()
    val pullRequestReviewer = PullRequestLinter.create()
    pullRequestReviewer.review(pullRequestId)
}


