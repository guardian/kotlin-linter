package com.guardian.ktlinter.ktlint

import com.guardian.ktlinter.ktlint.models.KtLintReport
import com.guardian.ktlinter.models.Patch
import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.reports.ReportCreator
import com.guardian.ktlinter.reports.models.Report
import com.guardian.ktlinter.reports.models.SuggestedChange

class KtLintReportCreator(
    private val runKtLintOnDirectory: RunKtLintOnDirectory,
    private val parseKtLintReport: ParseKtLintReport
) : ReportCreator {

    private val reportLocation: String = "report"

    override fun create(pullRequest: PullRequest): Report {
        val reportLocation = runKtLintOnDirectory(pullRequest, pullRequest.localFiles, reportLocation)
        val ktLintReport = parseKtLintReport(reportLocation)
        val suggestedChanges =
            createSuggestedChanges(pullRequest.files.map { it.fetchedFile.patches }.flatten(), ktLintReport)
        return Report(
            pullRequest,
            suggestedChanges
        )
    }

    private fun createSuggestedChanges(patches: List<Patch>, report: KtLintReport): List<SuggestedChange> {
        return report.fileReports.map { fileReport ->
            val patchesForFile = patches.filter { fileReport.file.contains(it.fileName) }
            patchesForFile
                .filterNot { patch -> patch.additions.isEmpty() }
                .flatMap { patch ->
                    patch.additions.flatMap { addition ->
                        val matchingErrors = fileReport.errors
                            .filter { ktLintError -> ktLintError.line == addition.lineInFile }

                        matchingErrors.map { ktLintError ->
                            SuggestedChange(
                                patch.fileName,
                                patch.commitId,
                                addition.lineInFile,
                                ktLintError.message
                            )
                        }
                    }

                }
        }.flatten()
    }
}

