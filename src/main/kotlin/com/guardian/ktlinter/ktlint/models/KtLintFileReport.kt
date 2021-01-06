package com.guardian.ktlinter.ktlint.models

import com.guardian.ktlinter.ktlint.models.KtLintError

data class KtLintFileReport(
    val file: String,
    val errors: List<KtLintError>

)
