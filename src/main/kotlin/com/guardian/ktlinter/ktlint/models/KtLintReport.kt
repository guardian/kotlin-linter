package com.guardian.ktlinter.ktlint.models

data class KtLintReport(
    val fileReports: List<KtLintFileReport>
)