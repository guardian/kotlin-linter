package com.guardian.ktlinter.ktlint

data class KtLintFileReport(
    val file: String,
    val errors: List<KtLintError>

)
