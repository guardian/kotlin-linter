package com.guardian.ktlinter.github.models

data class GithubPullRequest(
    val head: GithubCommit,
    val number: Int
)
