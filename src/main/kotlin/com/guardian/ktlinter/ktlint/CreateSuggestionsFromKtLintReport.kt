package com.guardian.ktlinter.ktlint

import com.guardian.ktlinter.ktlint.models.KtLintReport
import com.guardian.ktlinter.models.Patch
import com.guardian.ktlinter.reports.models.SuggestedChange

class CreateSuggestionsFromKtLintReport {

    operator fun invoke(ktLintFileReport: KtLintReport, patches: List<Patch>): List<SuggestedChange> {
        return ktLintFileReport.fileReports.map { fileReport ->
            val patchesForFile = patches.filter { fileReport.file.contains(it.fileName) }
            patchesForFile
                .filterNot { patch -> patch.additions.isEmpty() }
                .flatMap { patch ->
                    patch.additions.flatMap { addition ->
                        val matchingErrors = fileReport.errors
                            .filter { ktLintError -> ktLintError.line == addition.lineInFile }

                        matchingErrors.map {
                            SuggestedChange(
                                patch.fileName,
                                patch.commitId,
                                addition.lineInFile,
                                it
                            )
                        }
                    }

                }

        }.flatten()
    }
}
