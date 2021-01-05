package com.guardian.ktlinter.ktlint

data class KtLintError(
    val line: Int,
    val column: Int,
    val message: String,
    val rule: String
)