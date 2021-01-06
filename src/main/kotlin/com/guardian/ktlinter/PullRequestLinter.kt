package com.guardian.ktlinter

import com.google.gson.GsonBuilder
import com.guardian.ktlinter.git.GetPatchMetaData
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.GithubPullRequestFileFetcher
import com.guardian.ktlinter.github.GithubPullRequestReviewer
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.github.usecases.DownloadRelevantPullRequestFiles
import com.guardian.ktlinter.github.usecases.RetrievePullRequestFiles
import com.guardian.ktlinter.ktlint.KtLintReportCreator
import com.guardian.ktlinter.ktlint.ParseKtLintReport
import com.guardian.ktlinter.ktlint.RunKtLintOnDirectory
import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.models.Value
import com.guardian.ktlinter.pullrequests.PullRequestFileFetcher
import com.guardian.ktlinter.reports.ReportCreator
import com.guardian.ktlinter.reviews.PullRequestReviewer

class PullRequestLinter(
    private val pullRequestFileFetcher: PullRequestFileFetcher,
    private val reportCreator: ReportCreator,
    private val pullRequestReviewer: PullRequestReviewer
) {

    fun review(pullRequestNumber: Int) {
        when (val prValue = pullRequestFileFetcher.fetch(pullRequestNumber)) {
            is Value.Data<*> -> {
                val pullRequest = prValue.data as PullRequest
                val report = reportCreator.create(pullRequest)
                pullRequestReviewer.review(pullRequest, report)
            }
            is Value.Error -> println(prValue.message)
        }
    }

    companion object {
        fun create(): PullRequestLinter {
            val gson = GsonBuilder().setLenient().create()
            val gitHubService =
                GitHubService.create(LinterCredentials.GITHUB_USERNAME, LinterCredentials.GITHUB_TOKEN, gson)

            return PullRequestLinter(
                GithubPullRequestFileFetcher(
                    gitHubService,
                    RetrievePullRequestFiles(gitHubService, ParseGitPatchIntoLines(GetPatchMetaData())),
                    DownloadRelevantPullRequestFiles(gitHubService)
                ),
                KtLintReportCreator(RunKtLintOnDirectory(), ParseKtLintReport(gson)),
                GithubPullRequestReviewer(gitHubService)
            )
        }
    }
}