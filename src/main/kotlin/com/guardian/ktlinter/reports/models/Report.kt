package com.guardian.ktlinter.reports.models

import com.guardian.ktlinter.models.PullRequest

data class Report(
    val pullRequest: PullRequest,
    val suggestedChanges: List<SuggestedChange>
)