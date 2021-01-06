package com.guardian.ktlinter.github.models

data class GithubReview(
    val body: String,
    val event: String = "RIGHT",
    val comments: List<GithubComment>
)
