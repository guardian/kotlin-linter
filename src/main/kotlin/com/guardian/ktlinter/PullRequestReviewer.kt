package com.guardian.ktlinter

import com.google.gson.GsonBuilder
import com.guardian.ktlinter.git.GetPatchMetaData
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.*
import com.guardian.ktlinter.ktlint.KtLintReport
import com.guardian.ktlinter.ktlint.ParseKtLintReport
import com.guardian.ktlinter.ktlint.RunKtLintOnDirectory

class PullRequestReviewer(
    private val config: Config,
    private val pullRequestFileFetcher: PullRequestFileFetcher,
    private val runKtLintOnDirectory: RunKtLintOnDirectory,
    private val parseKtLintReport: ParseKtLintReport,
    private val createCommentsFromKtLintErrors: CreateCommentsFromKtLintErrors,
    private val createAPullRequestReview: CreateAPullRequestReview
) {

    fun review(pullRequestNumber: Int) {


        when (val prValue = pullRequestFileFetcher.fetch(pullRequestNumber)) {
            is Value.Data<*> -> {
                val pullRequest = prValue.data as PullRequest
                runKtLintReport(pullRequest)
            }
            is Value.Error -> println(prValue.message)
        }
    }


    private fun runKtLintReport(pullRequest: PullRequest) {
        val reportLocation = runKtLintOnDirectory(pullRequest, config.fileLocation, config.reportLocation)
        val report = parseKtLintReport.invoke(reportLocation)
        createGithubReview(pullRequest, report)
    }


    private fun createGithubReview(pullRequest: PullRequest, report: KtLintReport) {
        val suggestedChanges =
            createCommentsFromKtLintErrors(report, pullRequest.files.map { it.fetchedFile.patches }.flatten())

        val review = if (suggestedChanges.isEmpty()) {
            PostAReview(
                "Ktlint has not suggested any changes. 👍",
                "COMMENT",
                emptyList()
            )
        } else {
            PostAReview(
                "Hello, checkout the comments from ktlint.",
                "COMMENT",
                suggestedChanges.map { it.toReviewComment() }
            )
        }
        when (val reviewValue = createAPullRequestReview.invoke(pullRequest, review)) {
            is Value.Data<*> -> println("Review is successfully submitted.")
            is Value.Error -> println(reviewValue.message)
        }
    }

    data class Config(
        val fileLocation: String,
        val reportLocation: String
    )

    companion object {
        fun create(config: Config): PullRequestReviewer {

            val gson = GsonBuilder().setLenient().create()
            val gitHubService =
                GitHubService.create(LinterCredentials.GITHUB_USERNAME, LinterCredentials.GITHUB_TOKEN, gson)

            return PullRequestReviewer(
                config,
                GithubPullRequestFileFetcher(
                    GetPullRequestDetails(gitHubService),
                    GetGithubPullRequestFiles(gitHubService, ParseGitPatchIntoLines(GetPatchMetaData())),
                    DownloadRelevantPullRequestFiles(gitHubService)
                ),
                RunKtLintOnDirectory(),
                ParseKtLintReport(gson),
                CreateCommentsFromKtLintErrors(),
                CreateAPullRequestReview(gitHubService)
            )
        }
    }

    private fun SuggestedChange.toReviewComment(): ReviewComment {
        val comment = ktLintError.message
        return ReviewComment(
            comment,
            file,
            line
        )
    }
}