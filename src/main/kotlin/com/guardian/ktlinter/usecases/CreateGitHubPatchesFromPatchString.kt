package com.guardian.ktlinter.usecases

class CreateGitHubPatchesFromPatchString {

    operator fun invoke(filename: String, sha: String, patches: String): List<LineWithAddition> {
        val individualPatches = patches
            .split("@@ -")
            .filterNot { s -> s.isEmpty() }

        return individualPatches.map { patch ->
            val linesInPatch = patch.split("\n").map { s -> s.trim() }

            val getFirstLineInFilePatchAppliesTo = linesInPatch[0]
                .split("@@").first()
                .split(" ").first().split(",").first().toInt() - 1

            println(getFirstLineInFilePatchAppliesTo)
            val linesWithoutPatch = linesInPatch.filterNot { s -> s.startsWith("@@") }
            val linesWithoutDeletions = linesWithoutPatch.filterNot { s -> s.startsWith("-") }

            linesWithoutDeletions
                .mapIndexedNotNull { index, s ->
                    if (s.startsWith("+")) {
                        LineWithAddition(
                            filename = filename,
                            position = index,
                            lineInFile = getFirstLineInFilePatchAppliesTo + index,
                            commitId = sha
                        )
                    } else {
                        null
                    }
                }
        }.flatten()
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