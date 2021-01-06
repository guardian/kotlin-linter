package com.guardian.ktlinter.models
data class Patch(
    val fileName: String,
    val commitId: String,
    val lines: List<PatchLine>
) {
    val additions: List<PatchLine.Addition>
        get() = lines.filterIsInstance(PatchLine.Addition::class.java)
}
