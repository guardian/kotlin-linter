package com.guardian.ktlinter

interface PullRequestFileFetcher {

    fun fetch(id: Int): Value

}
