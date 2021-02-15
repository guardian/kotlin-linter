package com.theguardian.ktlinter.changerequests

import com.theguardian.ktlinter.changerequests.github.GetPatchMetaData
import com.theguardian.ktlinter.changerequests.github.GitHubRepositoryService
import com.theguardian.ktlinter.changerequests.github.ParseGitPatchIntoLines
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import retrofit2.Retrofit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ExtendWith(ContinuationParameterResolver::class)
internal class GithubRetrieveChangeRequestTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var githubRepositoryService: GitHubRepositoryService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        githubRepositoryService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .build()
            .create(GitHubRepositoryService::class.java)
    }

    @Test
    fun `Test all mocks are available`() {
        assertNotNull(readFile("resources/mock_pull_request.json"))
        assertNotNull(readFile("resources/mock_pull_request_files.json"))
    }

    @Test
    suspend fun `Test a ChangeRequest is created`(continuation: Continuation<*>) {
        val githubRetrieveChangeRequest = GithubRetrieveChangeRequest(
            githubRepositoryService, ParseGitPatchIntoLines(
                GetPatchMetaData()
            )
        )
        githubRetrieveChangeRequest.retrieve("100")
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun readFile(path: String): String {
        return javaClass.classLoader?.getResourceAsStream(path)?.source()?.use { source ->
            source.buffer().use { bufferedSource ->
                generateSequence { bufferedSource.readUtf8() }.joinToString()
            }
        } ?: ""
    }
}

class ContinuationParameterResolver : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == Continuation::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Continuation<Any?> {
        return object : Continuation<Any?> {
            override fun resumeWith(result: Result<Any?>) {

            }

            override val context: CoroutineContext
                get() = EmptyCoroutineContext
        }
    }
}
