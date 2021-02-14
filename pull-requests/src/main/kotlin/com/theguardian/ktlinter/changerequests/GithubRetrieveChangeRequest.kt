package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.data.ChangeRequest

class GithubRetrieveChangeRequest : RetrieveChangeRequest {
    override suspend fun retrieve(changeRequestId: String): ChangeRequest {
        TODO("Must implement.")
    }
}