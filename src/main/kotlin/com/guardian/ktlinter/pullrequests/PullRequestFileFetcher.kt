package com.guardian.ktlinter.pullrequests

import com.guardian.ktlinter.models.Value

interface PullRequestFileFetcher {

    fun fetch(id: Int): Value

}
