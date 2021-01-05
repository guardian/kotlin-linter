package com.guardian.ktlinter.ktlint

data class KtLintReport(
    val fileReports: List<KtLintFileReport>
)