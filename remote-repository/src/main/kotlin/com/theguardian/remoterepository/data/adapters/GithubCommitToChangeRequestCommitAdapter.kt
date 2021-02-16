package com.theguardian.remoterepository.data.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.theguardian.remoterepository.data.*

internal class GithubPullRequestToChangeRequestDetails {

    @FromJson
    fun changeRequestFromGithubPullRequest(githubPullRequest: GithubPullRequest): ChangeRequestDetails {
        return githubPullRequest.toChangeRequestDetails()
    }

    @ToJson
    fun changeRequestToGithubPullRequest(changeRequestDetails: ChangeRequestDetails): GithubPullRequest {
        return changeRequestDetails.toGithubPullRequest()
    }

    private fun ChangeRequestDetails.toGithubPullRequest(): GithubPullRequest {
        return GithubPullRequest(
            number = number,
            head = head.toGithubCommit(),
            user = user.toGithubUser()
        )
    }

    private fun ChangeRequestCommit.toGithubCommit(): GithubCommit {
        return GithubCommit(
            ref = ref,
            sha = sha
        )
    }

    private fun GithubPullRequest.toChangeRequestDetails(): ChangeRequestDetails {
        return ChangeRequestDetails(
            number = number,
            head = head.toChangeRequestCommit(),
            user = user.toChangeRequestUser(),
            branch = head.ref
        )
    }

    private fun ChangeRequestUser.toGithubUser(): GithubUser {
        return GithubUser(
            login = username
        )
    }

    private fun GithubCommit.toChangeRequestCommit(): ChangeRequestCommit {
        return ChangeRequestCommit(
            ref = ref,
            sha = sha
        )
    }

    private fun GithubUser.toChangeRequestUser(): ChangeRequestUser {
        return ChangeRequestUser(
            username = login
        )
    }
}