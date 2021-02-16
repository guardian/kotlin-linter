package com.theguardian.remoterepository.data

internal data class GithubPullRequest(
    val head: GithubCommit,
    val number: Int,
    val user: GithubUser
)
