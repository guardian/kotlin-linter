package com.guardian.ktlinter.usecases

class GetPatchMetaData {
    operator fun invoke(patch: String): PatchMetaData {
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

data class PatchMetaData(
    val patchStartLine: Int,
    val patchLength: Int
)