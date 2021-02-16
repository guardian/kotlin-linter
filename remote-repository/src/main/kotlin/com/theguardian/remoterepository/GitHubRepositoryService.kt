package com.theguardian.remoterepository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.theguardian.remoterepository.data.ChangeRequestDetails
import com.theguardian.remoterepository.data.GithubPullRequestFile
import com.theguardian.remoterepository.data.adapters.GithubPullRequestToChangeRequestDetails
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    ): ChangeRequestDetails

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
            baseUrl: HttpUrl = HttpUrl.parse("https://api.github.com/repos/")!!
        ): GitHubRepositoryService {
            val moshi = Moshi.Builder()
                .add(GithubPullRequestToChangeRequestDetails())
                .addLast(KotlinJsonAdapterFactory())
                .build()
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
                .baseUrl("$baseUrl$user/$project/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(GitHubRepositoryService::class.java)
        }
    }

}
