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
    private val getPullRequestDetails: GetPullRequestDetails,
    private val getPullRequestFiles: GetPullRequestFiles,
    private val downloadRelevantPullRequestFiles: DownloadRelevantPullRequestFiles,
    private val runKtLintOnDirectory: RunKtLintOnDirectory,
    private val parseKtLintReport: ParseKtLintReport,
    private val parseGitPatchIntoLines: ParseGitPatchIntoLines,
    private val createCommentsFromKtLintErrors: CreateCommentsFromKtLintErrors,
    private val createAPullRequestReview: CreateAPullRequestReview
) {

    fun review(pullRequestNumber: Int) {
        when (val prValue = getPullRequestDetails(pullRequestNumber)) {
            is Value.Data<*> -> downloadFiles(prValue.data as PullRequest)
            is Value.Error -> println(prValue.message)
        }
    }


    private fun downloadFiles(pullRequest: PullRequest) {
        when (val files = getPullRequestFiles(pullRequest)) {
            is Value.Data<*> -> downloadRelevantFiles(pullRequest, files.data as List<PullRequestFile>)
            is Value.Error -> println(files.message)
        }
    }

    private fun downloadRelevantFiles(pullRequest: PullRequest, files: List<PullRequestFile>) {
        downloadRelevantPullRequestFiles(pullRequest.head.ref, config.fileLocation + pullRequest.number + "/", files)
        runKtLintReport(pullRequest, files)
    }

    private fun runKtLintReport(pullRequest: PullRequest, files: List<PullRequestFile>) {
        val reportLocation = runKtLintOnDirectory(pullRequest, config.fileLocation, config.reportLocation)
        val report = parseKtLintReport.invoke(reportLocation)
        createGithubReview(pullRequest, report, files)
    }


    private fun createGithubReview(pullRequest: PullRequest, report: KtLintReport, files: List<PullRequestFile>) {
        val patchesByFile = files.map { kotlinFile ->
            kotlinFile.patch.split("@@ -").filterNot { s -> s.isEmpty() }.map {
                parseGitPatchIntoLines.invoke(kotlinFile.filename, kotlinFile.sha, it)
            }
        }.flatten()
        val suggestedChanges = createCommentsFromKtLintErrors(report, patchesByFile)

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
                GetPullRequestDetails(gitHubService),
                GetPullRequestFiles(gitHubService),
                DownloadRelevantPullRequestFiles(gitHubService),
                RunKtLintOnDirectory(),
                ParseKtLintReport(gson),
                ParseGitPatchIntoLines(GetPatchMetaData()),
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