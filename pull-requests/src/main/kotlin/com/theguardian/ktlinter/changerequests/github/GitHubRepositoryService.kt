package com.theguardian.ktlinter.changerequests.github

import com.google.gson.Gson
import com.theguardian.ktlinter.changerequests.github.data.GithubPullRequest
import com.theguardian.ktlinter.changerequests.github.data.GithubPullRequestFile
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

internal interface GitHubRepositoryService {

    @GET("pulls/{pull_number}/files")
    suspend fun getPullRequestFiles(
        @Path("pull_number") pullRequestNumber: Int
    ): List<GithubPullRequestFile>

    @GET("pulls/{pull_number}")
    suspend fun getPullRequestDetails(
        @Path("pull_number") pullRequestNumber: Int
    ): GithubPullRequest

    @GET("contents/{path}")
    @Headers("Content-Type: application/json")
    suspend fun getFileContents(
        @Path("path") path: String,
        @Query("ref") ref: String
    ): String

    companion object {

        fun create(
            githubUsername: String,
            githubToken: String,
            user: String,
            project: String,
            gson: Gson
        ): GitHubRepositoryService {
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
                .baseUrl("https://api.github.com/repos/$user/$project/")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(GitHubRepositoryService::class.java)
        }
    }

}
