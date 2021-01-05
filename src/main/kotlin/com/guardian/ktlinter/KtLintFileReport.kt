package com.guardian.ktlinter

data class KtLintFileReport(
    val file: String,
    val errors: List<KtLintError>

)
