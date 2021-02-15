package com.theguardian.ktlinter.changerequests.data

data class Patch(
    val fileName: String,
    val commitId: String,
    val lines: List<Line>
) {

    val additions: List<Line.Addition>
        get() = lines.filterIsInstance(Line.Addition::class.java)

    val deletions: List<Line.Removal>
        get() = lines.filterIsInstance(Line.Removal::class.java)

    val noChanges: List<Line.NoChange>
        get() = lines.filterIsInstance(Line.NoChange::class.java)

    sealed class Line {
        object NoChange : Line()
        data class Removal(
            val change: String
        ) : Line()

        object Metadata : Line()
        data class Addition(
            val lineInFile: Int,
            val lineInPatch: Int,
            val change: String
        ) : Line()
    }
}