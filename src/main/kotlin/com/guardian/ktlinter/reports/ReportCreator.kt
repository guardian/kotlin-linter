package com.guardian.ktlinter.reports

import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.reports.models.Report


interface ReportCreator {

    fun create(pullRequest: PullRequest): Report

}