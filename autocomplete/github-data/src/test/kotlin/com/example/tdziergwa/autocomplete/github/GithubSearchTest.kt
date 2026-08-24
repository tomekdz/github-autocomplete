package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.domain.SearchException
import com.example.tdziergwa.autocomplete.domain.SearchOutcome
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

private class FakeApi(
    private val users: Result<List<GithubResult.User>> = Result.success(emptyList()),
    private val repositories: Result<List<GithubResult.Repository>> = Result.success(emptyList()),
) : GithubSearchApi {

    var lastPerPage = 0

    override suspend fun searchUsers(query: String, perPage: Int): List<GithubResult.User> {
        lastPerPage = perPage
        return users.getOrThrow()
    }

    override suspend fun searchRepositories(query: String, perPage: Int): List<GithubResult.Repository> {
        lastPerPage = perPage
        return repositories.getOrThrow()
    }
}

private fun rateLimited() = SearchException(SearchError.RateLimited(30))

class GithubSearchTest {

    @Test
    @DisplayName("merges both endpoints into one alphabetical list")
    fun mergesAndSorts() = runTest {
        val api = FakeApi(
            users = Result.success(listOf(user(1, "zeta"), user(2, "beta"))),
            repositories = Result.success(listOf(repo(3, "Alpha"), repo(4, "gamma"))),
        )

        val outcome = GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)

        assertInstanceOf(SearchOutcome.Complete::class.java, outcome)
        assertEquals(listOf("Alpha", "beta", "gamma", "zeta"), outcome.items.map { it.sortKey })
    }

    @Test
    @DisplayName("keeps at most the limit after the merge")
    fun appliesTheLimit() = runTest {
        val api = FakeApi(
            users = Result.success((1..40).map { user(it.toLong(), "user%02d".format(it)) }),
            repositories = Result.success((41..80).map { repo(it.toLong(), "repo%02d".format(it)) }),
        )

        val outcome = GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)

        assertEquals(50, outcome.items.size)
    }

    @Test
    @DisplayName("asks each endpoint for the number of items the strategy chose")
    fun honoursTheStrategy() = runTest {
        val api = FakeApi()

        GithubSearch(api, FetchStrategy.Split).search("abc", 50)

        assertEquals(25, api.lastPerPage)
    }

    @Test
    @DisplayName("a failed repository search still shows the users")
    fun reportsUsersOnly() = runTest {
        val api = FakeApi(
            users = Result.success(listOf(user(1, "kotlin"))),
            repositories = Result.failure(rateLimited()),
        )

        val outcome = GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)

        assertInstanceOf(SearchOutcome.UsersOnly::class.java, outcome)
        assertEquals(listOf("kotlin"), outcome.items.map { it.sortKey })
    }

    @Test
    @DisplayName("a failed user search still shows the repositories")
    fun reportsReposOnly() = runTest {
        val api = FakeApi(
            users = Result.failure(IOException("offline")),
            repositories = Result.success(listOf(repo(1, "kotlin"))),
        )

        val outcome = GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)

        assertInstanceOf(SearchOutcome.ReposOnly::class.java, outcome)
        assertEquals(SearchError.Network, (outcome as SearchOutcome.ReposOnly).reason)
    }

    @Test
    @DisplayName("a partial success keeps an empty list when the half that worked matched nothing")
    fun allowsAnEmptyPartialResult() = runTest {
        val api = FakeApi(
            users = Result.success(emptyList()),
            repositories = Result.failure(rateLimited()),
        )

        val outcome = GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)

        assertInstanceOf(SearchOutcome.UsersOnly::class.java, outcome)
        assertTrue(outcome.items.isEmpty())
    }

    @Test
    @DisplayName("two failed endpoints throw, and a spent quota wins over the other cause")
    fun throwsWhenBothFail() = runTest {
        val api = FakeApi(
            users = Result.failure(IOException("offline")),
            repositories = Result.failure(rateLimited()),
        )

        val thrown = assertThrows<SearchException> {
            GithubSearch(api, FetchStrategy.Overfetch).search("abc", 50)
        }

        assertInstanceOf(SearchError.RateLimited::class.java, thrown.error)
    }
}
