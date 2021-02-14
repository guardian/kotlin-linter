package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.data.ChangeRequest

/**
 * Retrieve a change request from a source.
 */
interface RetrieveChangeRequest {

    /**
     * Retriece a change request using the [changeRequestId] provided to the function.
     *
     * @return a [ChangeRequest]
     */
    suspend fun retrieve(changeRequestId: String): ChangeRequest
}