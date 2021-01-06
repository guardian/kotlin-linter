package com.guardian.ktlinter.models

data class FetchedFile(
    val filename: String,
    val rawFileUrl: String,
    val patches: List<Patch>
)