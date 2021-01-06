package com.guardian.ktlinter

import com.google.gson.GsonBuilder
import com.guardian.ktlinter.git.GetPatchMetaData
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.GithubPullRequestFileFetcher
import com.guardian.ktlinter.github.models.GithubComment
import com.guardian.ktlinter.github.models.GithubReview
import com.guardian.ktlinter.github.network.GitHubService
import com.guardian.ktlinter.github.usecases.DownloadRelevantPullRequestFiles
import com.guardian.ktlinter.github.usecases.PostGithubPullRequestReview
import com.guardian.ktlinter.github.usecases.RetrievePullRequestFiles
import com.guardian.ktlinter.ktlint.KtLintReportCreator
import com.guardian.ktlinter.ktlint.ParseKtLintReport
import com.guardian.ktlinter.ktlint.RunKtLintOnDirectory
import com.guardian.ktlinter.models.PullRequest
import com.guardian.ktlinter.reports.ReportCreator
import com.guardian.ktlinter.reports.models.Report
import com.guardian.ktlinter.reports.models.SuggestedChange

class PullRequestReviewer(
    private val pullRequestFileFetcher: PullRequestFileFetcher,
    private val reportCreator: ReportCreator,
    private val postGithubPullRequestReview: PostGithubPullRequestReview
) {

    fun review(pullRequestNumber: Int) {
        when (val prValue = pullRequestFileFetcher.fetch(pullRequestNumber)) {
            is Value.Data<*> -> {
                val pullRequest = prValue.data as PullRequest
                val report = reportCreator.create(pullRequest)
                createGithubReview(pullRequest, report)
            }
            is Value.Error -> println(prValue.message)
        }
    }


    private fun createGithubReview(pullRequest: PullRequest, report: Report) {

        val review = if (report.suggestedChanges.isEmpty()) {
            GithubReview(
                "Ktlint has not suggested any changes. 👍",
                "COMMENT",
                emptyList()
            )
        } else {
            GithubReview(
                "Hello, checkout the comments from ktlint.",
                "COMMENT",
                report.suggestedChanges.map { it.toReviewComment() }
            )
        }
        when (val reviewValue = postGithubPullRequestReview.invoke(pullRequest, review)) {
            is Value.Data<*> -> println("Review is successfully submitted.")
            is Value.Error -> println(reviewValue.message)
        }
    }

    companion object {
        fun create(): PullRequestReviewer {

            val gson = GsonBuilder().setLenient().create()
            val gitHubService =
                GitHubService.create(LinterCredentials.GITHUB_USERNAME, LinterCredentials.GITHUB_TOKEN, gson)

            return PullRequestReviewer(
                GithubPullRequestFileFetcher(
                    gitHubService,
                    RetrievePullRequestFiles(gitHubService, ParseGitPatchIntoLines(GetPatchMetaData())),
                    DownloadRelevantPullRequestFiles(gitHubService)
                ),
                KtLintReportCreator(RunKtLintOnDirectory(), ParseKtLintReport(gson)),
                PostGithubPullRequestReview(gitHubService)
            )
        }
    }

    private fun SuggestedChange.toReviewComment(): GithubComment {
        return GithubComment(suggestion, file, line)
    }
}