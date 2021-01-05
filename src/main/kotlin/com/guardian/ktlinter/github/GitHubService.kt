package com.guardian.ktlinter.github

import retrofit2.Call
import retrofit2.http.*

interface GitHubService {

    @GET("repos/guardian/android-news-app/pulls/{pull_number}/files")
    fun getPullRequestFiles(
        @Path("pull_number") pullRequestNumber: String
    ): Call<List<PullRequestFile>>

    @GET("repos/guardian/android-news-app/pulls/{pull_number}")
    fun getPullRequestDetails(
        @Path("pull_number") pullRequestNumber: String
    ): Call<PullRequest>

    @GET("repos/guardian/android-news-app/contents/{path}")
    @Headers("Content-Type: application/json")
    fun getFileContents(@Path("path") path: String, @Query("ref") ref: String): Call<String>

    // https://docs.github.com/en/free-pro-team@latest/rest/reference/pulls#create-a-review-comment-for-a-pull-request
    @POST("repos/guardian/android-news-app/pulls/{pull_number}/comments")
    @Headers("Accept: application/vnd.github.v3+json")
    fun postAComment(
        @Path("pull_number") pullNumber: String,
        @Body postACommentRequest: PostACommentRequest
    ): Call<String>

    // https://docs.github.com/en/free-pro-team@latest/rest/reference/pulls#create-a-review-for-a-pull-request
    @POST("repos/guardian/android-news-app/pulls/{pull_number}/reviews")
    @Headers("Accept: application/vnd.github.v3+json")
    fun postAReview(
        @Path("pull_number") pullNumber: String,
        @Body postAReview: PostAReview
    ): Call<String>
}

data class PullRequest(
    val head: Commit
)

class Commit(
    val ref: String,
    val sha: String
)

data class PostACommentRequest(
    val body: String,
    val path: String,
    val line: Int,
    val commit_id: String,
    val side: String = "RIGHT"
)

data class PostAReview(
    val body: String,
    val event: String = "RIGHT",
    val comments: List<ReviewComment>
)

data class ReviewComment(
    val body: String,
    val path: String,
    val line: Int,
    val side: String = "RIGHT"
)