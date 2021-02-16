package com.theguardian.remoterepository.data

data class ChangeRequestDetails(
    val head: ChangeRequestCommit,
    val number: Int,
    val branch: String,
    val user: ChangeRequestUser
)
