package com.guardian.ktlinter.github

data class ReviewComment(
    val body: String,
    val path: String,
    val line: Int,
    val side: String = "RIGHT"
)