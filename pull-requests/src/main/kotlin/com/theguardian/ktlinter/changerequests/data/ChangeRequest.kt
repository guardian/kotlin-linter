package com.theguardian.ktlinter.changerequests.data

/**
 * A change request represents a set of changes are being made to a central branch.
 */
data class ChangeRequest(
    val id: Int,
    val owner: String,
    val head: Commit,
    val branch: Branch,
    val changedFiles: List<ChangedFile>
) {

    /**
     * A change request will often involve comparing changes in different streams of work or branches.
     */
    data class Branch(
        val name: String
    )

    /**
     * A change request will involve 2..n commits.
     */
    data class Commit(
        val sha: String
    )
}
