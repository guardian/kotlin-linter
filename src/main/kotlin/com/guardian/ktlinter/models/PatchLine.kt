package com.guardian.ktlinter.models
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