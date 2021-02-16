package com.theguardian.remoterepository

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.buffer
import okio.source
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class GithubRemoteRepositoryTest {

    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun `Test a ChangeRequest has the correct branch name`() = runBlocking {
        val changeRequest = createAGithubRemoteRepository().getChangeRequestDetails("100")
        assertEquals("new-topic", changeRequest.branch)
    }

    @Test
    fun `Test a ChangeRequest has the correct head sha`() = runBlocking {
        val changeRequest = createAGithubRemoteRepository().getChangeRequestDetails("100")
        assertEquals("6dcb09b5b57875f334f61aebed695e2e4193db5e", changeRequest.head.sha)
    }

//    @Test
//    fun `Test a ChangeRequest has the correct file count`() = runBlocking {
//        val changeRequest = createAGithubRetrieveChangeRequest().retrieve("100")
//        assertEquals(1, changeRequest.changedFiles.size)
//    }
//
//    @Test
//    fun `Test a ChangeRequest has the correct download url for changed file`() = runBlocking {
//        val changeRequest = createAGithubRetrieveChangeRequest().retrieve("100")
//        assertEquals(
//            "https://github.com/octocat/Hello-World/raw/6dcb09b5b57875f334f61aebed695e2e4193db5e/file1.txt",
//            changeRequest.changedFiles[0].rawFileUrl
//        )
//    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createAGithubRemoteRepository(): RemoteRepository {
        mockWebServer.dispatcher = createMockDispatcher(
            listOf(
                MockResponseData("pulls/100", 200, "mock_pull_request.json"),
                MockResponseData("pulls/100/files", 200, "mock_pull_request_files.json")
            )
        )

        return GithubRemoteRepository(
            GitHubRepositoryService.create("", "", "", "", mockWebServer.url("/"))
        )
    }

    private fun createMockDispatcher(dispatchers: List<MockResponseData>): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return dispatchers.firstOrNull { mockResponseData ->
                    request.path!!.endsWith(mockResponseData.path)
                }?.let { mockResponseData ->
                    MockResponse()
                        .setResponseCode(mockResponseData.responseCode)
                        .setBody(readFile(mockResponseData.responseFile))
                } ?: MockResponse().setResponseCode(404)
            }
        }
    }

    private data class MockResponseData(
        val path: String,
        val responseCode: Int,
        val responseFile: String
    )

    private fun readFile(file: String): String {
        val testResources = File("src/test/resources/$file")
        return testResources.source().buffer().readUtf8()
    }

}