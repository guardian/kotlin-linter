package com.guardian.ktlinter.reviews

import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.reports.models.Report

interface PullRequestReviewer {
    fun review(pullRequest: PullRequest, report: Report)
}