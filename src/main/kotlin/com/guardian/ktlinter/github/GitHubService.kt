package com.guardian.ktlinter.github

import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface GitHubService {

    @GET("repos/guardian/android-news-app/pulls/{pull_number}/files")
    fun getPullRequestFiles(
        @Path("pull_number") pullRequestNumber: Int
    ): Call<List<PullRequestFile>>

    @GET("repos/guardian/android-news-app/pulls/{pull_number}")
    fun getPullRequestDetails(
        @Path("pull_number") pullRequestNumber: Int
    ): Call<PullRequest>

    @GET("repos/guardian/android-news-app/contents/{path}")
    @Headers("Content-Type: application/json")
    fun getFileContents(@Path("path") path: String, @Query("ref") ref: String): Call<String>

    // https://docs.github.com/en/free-pro-team@latest/rest/reference/pulls#create-a-review-for-a-pull-request
    @POST("repos/guardian/android-news-app/pulls/{pull_number}/reviews")
    @Headers("Accept: application/vnd.github.v3+json")
    fun postAReview(
        @Path("pull_number") pullNumber: Int,
        @Body postAReview: PostAReview
    ): Call<PullRequestReview>

    companion object {

        fun create(githubUsername: String, githubToken: String, gson: Gson): GitHubService {
            val okHttpClient =
                OkHttpClient.Builder()
                    .addInterceptor(
                        AuthenticationInterceptor(
                            Credentials.basic(
                                githubUsername,
                                githubToken
                            )
                        )
                    )
                    .addInterceptor { chain ->
                        println(chain.request().url())
                        chain.proceed(chain.request())
                    }
                    .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(GitHubService::class.java)
        }
    }

}
