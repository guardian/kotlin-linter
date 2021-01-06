package com.guardian.ktlinter.github

import com.guardian.ktlinter.Patch
import com.guardian.ktlinter.ktlint.KtLintError
import com.guardian.ktlinter.ktlint.KtLintReport

class CreateCommentsFromKtLintErrors {

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


data class SuggestedChange(
    val file: String,
    val commit: String,
    val line: Int,
    val ktLintError: KtLintError
)