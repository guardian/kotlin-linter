package com.guardian.ktlinter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.guardian.ktlinter.usecases.CreateAPullRequestReview
import com.guardian.ktlinter.usecases.CreateGitHubPatchesFromPatchString
import com.guardian.ktlinter.usecases.LineWithAddition
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.FileReader
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {

    val username = "jordanterry"
    val token = LinterCredentials.GITHUB_KEY
    val pullRequestId = "6445"
    val reportLocation = "report/$pullRequestId/ktlint-$pullRequestId.json"
    val fileStore = "tmp/$pullRequestId/"

    val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthenticationInterceptor(Credentials.basic(username, token)))
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
    val createGitHubPatchesFromPatchString = CreateGitHubPatchesFromPatchString()


    val pullRequestCall = service.getPullRequestDetails(pullRequestId)
    val pullRequestResponse = pullRequestCall.execute()
    if (pullRequestResponse.isSuccessful) {
        val pullRequest = pullRequestResponse.body()

        val fileCall = service.getPullRequestFiles(pullRequestId)
        val files = fileCall.execute().body()
        val kotlinFiles = files
            ?.filter { file -> file.filename.endsWith(".kt") } ?: emptyList()
        downloadRelevantFiles(service, fileStore, kotlinFiles)

        runKtLint(fileStore, reportLocation)

        val report = readKtLintReport(gson, reportLocation)


        val lineChangesByFile = kotlinFiles.map { file ->
            val patches = file.patch.split("@@ -").filterNot { it.isEmpty() }
            patches.map { patch ->
                createGitHubPatchesFromPatchString(file.filename, file.sha, patch)
            }.flatten()
        }.flatten().groupBy { it.filename }

        println(lineChangesByFile)


        val errorsToSendToGithub = report.map { fileReport ->
            val key = lineChangesByFile.keys.firstOrNull { fileReport.file.contains(it) }
            if (key != null) {
                val relevantLines = lineChangesByFile[key]!!
                relevantLines.flatMap { patch ->
                    fileReport.errors.filter { ktLintError -> ktLintError.line == patch.lineInFile }
                        .map {
                            GithubComment(
                                patch, it
                            )
                        }
                }
            } else {
                emptyList()
            }
        }.flatten()



        println("There are ${errorsToSendToGithub.size} comments to post to Github.")

        val comments = errorsToSendToGithub.map { githubComment ->
            val comment =
                "This change is for line ${githubComment.ktLintError.line} of ${githubComment.lineWithAddition.filename}\n${githubComment.ktLintError.message}"
            ReviewComment(
                comment,
                githubComment.lineWithAddition.filename,
                githubComment.lineWithAddition.lineInFile
            )
        }


        createAPullRequestReview.invoke(
            pullRequestId,
            pullRequest!!.head.sha,
            "COMMENT",
            comments
        )

    } else {
        println("There has been an error retrieving pull request details.")
    }


}

fun runKtLint(fileStore: String, reportName: String) {
    // Thank you stackoverflow 🙏 https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
    // Have disabled indent rules for the time being. Our continuation indents aren't correct
    "./ktlint $fileStore --reporter=plain?group_by_file --verbose --reporter=json,output=$reportName".runCommand(
        File("./")
    )
}


fun readKtLintReport(gson: Gson, reportLocation: String): List<KtLintFileReport> {
    val reader = JsonReader(FileReader(reportLocation))
    return gson.fromJson(reader, object : TypeToken<List<KtLintFileReport?>?>() {}.type)
}

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}


fun downloadRelevantFiles(
    gitHubService: GitHubService,
    filesStore: String,
    pullRequestFiles: List<PullRequestFile>
) {
    pullRequestFiles.forEach { file ->

        val splitDirectories = file.filename.split("/")

        val directories = splitDirectories.toMutableList().let {
            it.removeAt(splitDirectories.lastIndex)
            it.joinToString("/")
        }
        val saveDirectory = "$filesStore$directories/"
        val fileName = saveDirectory + splitDirectories.last()

        try {
            Files.createDirectories(Paths.get(saveDirectory))
        } catch (exception: FileAlreadyExistsException) {
            println(exception)
        }
        val localFile = File(fileName)
        if (!localFile.exists()) {
            val contentCall = gitHubService.getFileContents(file.filename)
            val contentsRequest = contentCall.execute()
            if (contentsRequest.isSuccessful) {
                localFile.writeText(contentsRequest.body()!!)
            } else {
                println("Response code: ${contentsRequest.code()}")
            }

        } else {
            println("$fileName already exists.")
        }
    }
}


data class GithubPatch(
    val filename: String,
    val lineOfFile: Int,
    val lineOfFileStart: Int,
    val positionInPatch: Int,
    val commitId: String
)

data class GithubComment(
    val lineWithAddition: LineWithAddition,
    val ktLintError: KtLintError
) {
//    val positionInHunk: Int
//        get() = ktLintError.line - githubPatch.start
}