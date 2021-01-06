package com.guardian.ktlinter.github.models

data class GithubComment(
    val body: String,
    val path: String,
    val line: Int,
    val side: String = "RIGHT"
)