package com.theguardian.remoterepository

import com.theguardian.remoterepository.data.ChangeRequestDetails

interface RemoteRepository {

    suspend fun getChangeRequestDetails(changeRequestId: String): ChangeRequestDetails

    suspend fun getChangeRequestFiles(changeRequestId: String)
}

internal class GithubRemoteRepository(
    private val gitHubRepositoryService: GitHubRepositoryService
) : RemoteRepository {

    override suspend fun getChangeRequestDetails(changeRequestId: String): ChangeRequestDetails {
        return gitHubRepositoryService.getPullRequestDetails(changeRequestId.toInt())
    }

    override suspend fun getChangeRequestFiles(changeRequestId: String) {
        gitHubRepositoryService.getPullRequestFiles(changeRequestId.toInt())
    }
}

object RemoteRepositoryFactory {

    enum class Remote {
        GITHUB
    }

    fun create(remote: Remote): RemoteRepository {
        return when (remote) {
            Remote.GITHUB -> GithubRemoteRepository(GitHubRepositoryService.create("", "", "", ""))
        }
    }
}