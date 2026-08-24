package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.AutocompleteConfig.Companion.MAX_RESULTS
import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.domain.SearchException
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

private const val SLOW_ANSWER_SECONDS = 2L
private const val READ_TIMEOUT_MILLIS = 200L

private const val USERS_BODY = """
{
  "items": [
    {
      "id": 1,
      "login": "octocat",
      "avatar_url": "https://avatars.example/1",
      "html_url": "https://github.com/octocat",
      "score": 1.0
    }
  ]
}
"""

private const val REPOSITORIES_BODY = """
{
  "items": [
    {
      "id": 2,
      "name": "hello-world",
      "owner": { "login": "octocat" },
      "stargazers_count": 42,
      "html_url": "https://github.com/octocat/hello-world"
    }
  ]
}
"""

class RetrofitGithubSearchApiTest {

    @StartStop
    val server = MockWebServer()

    private fun api() = RetrofitGithubSearchApi(
        githubSearchService(OkHttpClient(), server.url("/").toString()),
    )

    @Test
    @DisplayName("reads a user page, and asks for the query and the page size")
    fun readsUsers() = runTest {
        server.enqueue(MockResponse(body = USERS_BODY))

        val users = api().searchUsers("octo", 25)

        assertEquals(
            listOf(
                GithubResult.User(
                    id = 1,
                    login = "octocat",
                    avatarUrl = "https://avatars.example/1",
                    htmlUrl = "https://github.com/octocat",
                ),
            ),
            users,
        )
        val request = server.takeRequest()
        assertEquals("/search/users?q=octo&per_page=25", request.target)
        assertEquals("application/vnd.github+json", request.headers["Accept"])
        assertEquals("2022-11-28", request.headers["X-GitHub-Api-Version"])
    }

    @Test
    @DisplayName("reads a repository page, and takes the owner from the nested object")
    fun readsRepositories() = runTest {
        server.enqueue(MockResponse(body = REPOSITORIES_BODY))

        val repositories = api().searchRepositories("hello", 50)

        assertEquals(
            listOf(
                GithubResult.Repository(
                    id = 2,
                    name = "hello-world",
                    ownerLogin = "octocat",
                    stars = 42,
                    htmlUrl = "https://github.com/octocat/hello-world",
                ),
            ),
            repositories,
        )
    }

    @Test
    @DisplayName("an empty page gives an empty list")
    fun readsAnEmptyPage() = runTest {
        server.enqueue(MockResponse(body = """{ "items": [] }"""))

        assertTrue(api().searchUsers("octo", 50).isEmpty())
    }

    @Test
    @DisplayName("a slow answer from both endpoints becomes a network failure")
    fun mapsATimeout() = runTest {
        repeat(2) {
            server.enqueue(
                MockResponse.Builder()
                    .body("""{ "items": [] }""")
                    .bodyDelay(SLOW_ANSWER_SECONDS, TimeUnit.SECONDS)
                    .build(),
            )
        }
        val impatient = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        val search = githubSearch(impatient, FetchStrategy.Split, server.url("/").toString())

        val thrown = assertThrows<SearchException> { search("kotlin", MAX_RESULTS) }

        assertEquals(SearchError.Network, thrown.error)
    }

    @Test
    @DisplayName("a query that GitHub rejects gives an empty list, and not a failure")
    fun ignoresARejectedQuery() = runTest {
        server.enqueue(MockResponse(code = 422, body = """{ "message": "Validation Failed" }"""))

        assertTrue(api().searchRepositories("a:b:c", 50).isEmpty())
    }
}
