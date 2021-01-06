package com.guardian.ktlinter.reports.models

import com.guardian.ktlinter.ktlint.models.KtLintError

data class SuggestedChange(
    val file: String,
    val commit: String,
    val line: Int,
    val suggestion: String
)