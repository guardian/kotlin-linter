package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.data.Patch
import com.theguardian.ktlinter.changerequests.data.PatchMetaData

/**
 * Converts a Git patch into a [Patch].
 */
class ParseGitPatchIntoLines {

    /**
     * @param filename associated with the patch string
     * @param sha the commit sha associated with the patch string
     * @param patchString the [String] to translate to a [Patch]
     */
    operator fun invoke(filename: String, sha: String, patchString: String): Patch {
        val linesInPatchAfterMeta = patchString
            .split("@@ -")
            .filterNot { s -> s.isEmpty() }

        val stringLinesMappedToPatchLines = linesInPatchAfterMeta.map { patch ->
            val linesInPatch = patch.split("\n").map { s -> s.trim() }

            val patchMetaData = getPatchMetaData(patch)

            val linesWithoutPatchInfo = linesInPatch.filterIndexed { i, _ -> i > 0 }
            var lineCount = 0
            val lineBeforeStartOfChanges = patchMetaData.patchStartLine - 1
            linesWithoutPatchInfo.map { patchLine ->
                when {
                    patchLine.startsWith("+") -> Patch.Line.Addition(
                        lineInFile = lineBeforeStartOfChanges + lineCount,
                        lineInPatch = lineCount,
                        change = patchLine
                    ).also {
                        lineCount++
                    }
                    patchLine.startsWith("-") -> Patch.Line.Removal(
                        change = patchLine
                    )
                    patchLine.startsWith("@@") -> Patch.Line.Metadata
                    else -> Patch.Line.NoChange.also {
                        lineCount++
                    }
                }
            }
        }.flatten()

        return Patch(
            filename, sha, stringLinesMappedToPatchLines
        )
    }

    private fun getPatchMetaData(patch: String): PatchMetaData {
        val patchModifiedData = patch
            .split("@@")
            .map { s -> s.trim() }
            .filterNot { s -> s.isEmpty() }
            .first()
            .split("+")
            .last()
            .split(",")
        assert(patchModifiedData.size == 2) { "The size of the modified data is incorrect." }
        return PatchMetaData(
            patchStartLine = patchModifiedData.first().toInt(),
            patchLength = patchModifiedData.last().toInt()
        )
    }
}
