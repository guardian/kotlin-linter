package com.guardian.ktlinter.usecases

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
                        PatchLine.Addition(
                            lineInFile = lineBeforeStartOfChanges + lineCount,
                            lineInPatch = lineCount,
                            change = patchLine
                        )
                    }
                    patchLine.startsWith("-") -> {
                        PatchLine.Removal
                    }
                    patchLine.startsWith("@@") -> {
                        PatchLine.Metadata
                    }
                    else -> {
                        lineCount++
                        PatchLine.NoChange
                    }
                }
            }
        }.flatten()

        return Patch(
            filename, sha, stringLinesMappedToPatchLines
        )
    }


}

/**
 * @param filename what file is this change in?
 * @param position what position in a patch is this change?
 * @param lineInFile what line of the parent file is this?
 * @param commitId the commit id
 */
data class LineWithAddition(
    val filename: String,
    val position: Int,
    val lineInFile: Int,
    val commitId: String
)

data class Patch(
    val fileName: String,
    val commitId: String,
    val lines: List<PatchLine>
)

sealed class PatchLine {
    object NoChange : PatchLine()
    object Removal : PatchLine()
    object Metadata : PatchLine()
    data class Addition(
        val lineInFile: Int,
        val lineInPatch: Int,
        val change: String
    ) : PatchLine()
}