package com.guardian.ktlinter

data class FileChanges(
    val name: String,
    private val patchedAreas: List<IntRange>
) {

    operator fun contains(line: Int): Boolean {
        return patchedAreas.any { area -> line in area }
    }
}
