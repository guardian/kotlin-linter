package com.guardian.ktlinter.models

data class PullRequest(
    val id: Int,
    val head: Commit,
    val branch: Branch,
    val localFiles: String,
    val files: List<DownloadedFile>
) {
    data class Branch(
        val name: String
    )

    data class Commit(
        val sha: String
    )
}

