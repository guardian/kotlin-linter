package com.guardian.ktlinter.github

data class PostAReview(
    val body: String,
    val event: String = "RIGHT",
    val comments: List<ReviewComment>
)
