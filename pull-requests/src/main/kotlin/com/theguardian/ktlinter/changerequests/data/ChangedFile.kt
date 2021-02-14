package com.theguardian.ktlinter.changerequests.data

data class ChangedFile(
    val filename: String,
    val rawFileUrl: String,
    val patches: List<Patch>
)