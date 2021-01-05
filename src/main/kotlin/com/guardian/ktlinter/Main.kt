package com.guardian.ktlinter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.guardian.ktlinter.git.GetPatchMetaData
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.github.*
import com.guardian.ktlinter.ktlint.KtLintFileReport
import com.guardian.ktlinter.ktlint.RunKtLintOnDirectory
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.FileReader


fun main(args: Array<String>) {

    val username = "jordanterry"
    val token = LinterCredentials.GITHUB_KEY
    val pullRequestId = "6445"
    val reportLocation = "report/$pullRequestId/ktlint-$pullRequestId.json"
    val fileStore = "tmp/$pullRequestId/"

    val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                AuthenticationInterceptor(
                    Credentials.basic(
                        username,
                        token
                    )
                )
            )
            .addInterceptor { chain ->
                println(chain.request().url())
                chain.proceed(chain.request())
            }
            .build()
    val gson = GsonBuilder().setLenient().create()
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val service = retrofit.create(GitHubService::class.java)

    val createAPullRequestReview = CreateAPullRequestReview(service)
    val parseGitPatchIntoLines = ParseGitPatchIntoLines(GetPatchMetaData())
    val downloadRelevantFiles =
        DownloadRelevantPullRequestFiles(service)
    val runKtLintOnDirectory = RunKtLintOnDirectory()
    val createCommentsFromKtLintErrors =
        CreateCommentsFromKtLintErrors()


    val pullRequestCall = service.getPullRequestDetails(pullRequestId)
    val pullRequestResponse = pullRequestCall.execute()
    if (pullRequestResponse.isSuccessful) {
        val pullRequest = pullRequestResponse.body()

        val fileCall = service.getPullRequestFiles(pullRequestId)
        val files = fileCall.execute().body()
        val kotlinFiles = files
            ?.filter { file -> file.filename.endsWith(".kt") } ?: emptyList()

        downloadRelevantFiles(pullRequest!!.head.ref, fileStore, kotlinFiles)
        runKtLintOnDirectory(fileStore, reportLocation)

        val report = readKtLintReport(gson, reportLocation)


        val patchesByFile = kotlinFiles.map { kotlinFile ->
            kotlinFile.patch.split("@@ -").filterNot { s -> s.isEmpty() }.map {
                parseGitPatchIntoLines.invoke(kotlinFile.filename, kotlinFile.sha, it)
            }
        }.flatten()

        val suggestChanges = createCommentsFromKtLintErrors(report, patchesByFile)
        createAPullRequestReview.invoke(
            pullRequestId,
            suggestChanges
        )

    } else {
        println("There has been an error retrieving pull request details.")
    }
}

fun readKtLintReport(gson: Gson, reportLocation: String): List<KtLintFileReport> {
    val reader = JsonReader(FileReader(reportLocation))
    return gson.fromJson(reader, object : TypeToken<List<KtLintFileReport?>?>() {}.type)
}
