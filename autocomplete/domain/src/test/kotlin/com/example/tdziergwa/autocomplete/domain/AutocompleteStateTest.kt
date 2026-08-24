package com.example.tdziergwa.autocomplete.domain

import app.cash.turbine.test
import com.example.tdziergwa.autocomplete.domain.AutocompleteConfig.Companion.MAX_RESULTS
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private const val SLOW_SEARCH_MILLIS = 1_000L
private val KOTLIN = GithubResult.User(1, "kotlin", null, "https://github.com/kotlin")
private val AAA = GithubResult.User(2, "aaa", null, "https://github.com/aaa")
private val BBB = GithubResult.User(3, "bbb", null, "https://github.com/bbb")

private class FakeGithubSearch(
    private val answer: (String) -> SearchOutcome = { SearchOutcome.Complete(listOf(resultFor(it))) },
    private val takes: Long = 0,
) {

    val started = mutableListOf<String>()
    val finished = mutableListOf<String>()
    val limits = mutableListOf<Int>()

    suspend fun search(query: String, limit: Int): SearchOutcome {
        started += query
        limits += limit
        delay(takes)
        finished += query
        return answer(query)
    }
}

private fun resultFor(query: String): GithubResult = when (query) {
    "kotlin" -> KOTLIN
    "aaa" -> AAA
    "bbb" -> BBB
    else -> GithubResult.User(query.hashCode().toLong(), query, null, "https://github.com/$query")
}

@OptIn(ExperimentalCoroutinesApi::class)
class AutocompleteStateTest {

    @Test
    @DisplayName("a query below the minimum length reaches no source")
    fun staysIdleBelowTheMinimum() = runTest {
        val search = FakeGithubSearch()
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("ko")
            advanceUntilIdle()

            expectNoEvents()
            assertTrue(search.started.isEmpty())
        }
    }

    @Test
    @DisplayName("fast input produces one search, and it uses the newest text")
    fun debouncesFastInput() = runTest {
        val search = FakeGithubSearch()
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("k")
            state.onQueryChange("ko")
            state.onQueryChange("kot")

            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(AutocompleteUiState.Success(listOf(resultFor("kot"))), awaitItem())
            assertEquals(listOf("kot"), search.started)
            assertEquals(listOf(MAX_RESULTS), search.limits)
        }
    }

    @Test
    @DisplayName("a new character cancels the search that is in progress")
    fun cancelsTheOldSearch() = runTest {
        val search = FakeGithubSearch(takes = SLOW_SEARCH_MILLIS)
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("aaa")
            advanceTimeBy(SLOW_SEARCH_MILLIS / 2)
            assertEquals(AutocompleteUiState.Loading, awaitItem())

            state.onQueryChange("bbb")
            assertEquals(AutocompleteUiState.Success(listOf(BBB)), awaitItem())
        }

        assertEquals(listOf("aaa", "bbb"), search.started)
        assertEquals(listOf("bbb"), search.finished)
    }

    @Test
    @DisplayName("a source that finds nothing gives the empty state")
    fun reportsAnEmptyResult() = runTest {
        val search = FakeGithubSearch(answer = { SearchOutcome.Complete(emptyList()) })
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("kotlin")

            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(AutocompleteUiState.Empty, awaitItem())
        }
    }

    @Test
    @DisplayName("a partial success keeps the half that worked, and the reason")
    fun reportsAPartialSuccess() = runTest {
        val search = FakeGithubSearch(answer = { SearchOutcome.UsersOnly(listOf(resultFor(it)), SearchError.Network) })
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("kotlin")

            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(
                AutocompleteUiState.UsersOnly(listOf(KOTLIN), SearchError.Network),
                awaitItem(),
            )
        }
    }

    @Test
    @DisplayName("refresh searches again with the query that the user already typed")
    fun refreshSearchesAgain() = runTest {
        val search = FakeGithubSearch()
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("kotlin")
            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(AutocompleteUiState.Success(listOf(KOTLIN)), awaitItem())

            state.refresh()
            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(AutocompleteUiState.Success(listOf(KOTLIN)), awaitItem())
        }

        assertEquals(listOf("kotlin", "kotlin"), search.started)
    }

    @Test
    @DisplayName("refresh below the minimum length reaches no source")
    fun refreshStaysIdleBelowTheMinimum() = runTest {
        val search = FakeGithubSearch()
        val state = AutocompleteState(search::search, backgroundScope)

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("ko")
            state.refresh()
            advanceUntilIdle()

            expectNoEvents()
            assertTrue(search.started.isEmpty())
        }
    }

    @Test
    @DisplayName("a source that throws gives the failed state, and keeps the category")
    fun reportsAFailure() = runTest {
        val state = AutocompleteState(
            search = { _, _ -> throw SearchException(SearchError.Unauthorized) },
            scope = backgroundScope,
        )

        state.uiState.test {
            assertEquals(AutocompleteUiState.Idle, awaitItem())

            state.onQueryChange("kotlin")

            assertEquals(AutocompleteUiState.Loading, awaitItem())
            assertEquals(AutocompleteUiState.Failed(SearchError.Unauthorized), awaitItem())
        }
    }
}
