package com.theguardian.ktlinter.changerequests.github

import com.theguardian.ktlinter.changerequests.data.Patch


class ParseGitPatchIntoLines(
    private val getPatchMetaData: GetPatchMetaData
) {

    operator fun invoke(filename: String, sha: String, patch: String): Patch {
        val linesInPatchAfterMeta = patch
            .split("@@ -")
            .filterNot { s -> s.isEmpty() }

        val stringLinesMappedToPatchLines = linesInPatchAfterMeta.map { patch ->
            val linesInPatch = patch.split("\n").map { s -> s.trim() }

            val patchMetaData = getPatchMetaData(patch)

            val linesWithoutPatchInfo = linesInPatch.filterIndexed { i, s -> i > 0 }
            var lineCount = 0
            val lineBeforeStartOfChanges = patchMetaData.patchStartLine - 1
            linesWithoutPatchInfo.mapIndexed { position, patchLine ->
                when {
                    patchLine.startsWith("+") -> {
                        lineCount++
                        Patch.Line.Addition(
                            lineInFile = lineBeforeStartOfChanges + lineCount,
                            lineInPatch = lineCount,
                            change = patchLine
                        )
                    }
                    patchLine.startsWith("-") -> {
                        Patch.Line.Removal
                    }
                    patchLine.startsWith("@@") -> {
                        Patch.Line.Metadata
                    }
                    else -> {
                        lineCount++
                        Patch.Line.NoChange
                    }
                }
            }
        }.flatten()

        return Patch(
            filename, sha, stringLinesMappedToPatchLines
        )
    }
}
