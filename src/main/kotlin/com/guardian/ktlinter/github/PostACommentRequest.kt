package com.guardian.ktlinter.github
data class PostACommentRequest(
    val body: String,
    val path: String,
    val line: Int,
    val commit_id: String,
    val side: String = "RIGHT"
)