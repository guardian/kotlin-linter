package com.guardian.ktlinter.github

data class PullRequest(
    val head: Commit,
    val number: Int
)
