package com.theguardian.ktlinter.changerequests.github.data

internal data class GithubPullRequest(
    val head: GithubCommit,
    val number: Int
)
