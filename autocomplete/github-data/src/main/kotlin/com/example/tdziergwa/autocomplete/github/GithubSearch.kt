package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.AutocompleteConfig.Companion.MAX_RESULTS
import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.domain.SearchException
import com.example.tdziergwa.autocomplete.domain.SearchOutcome
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

internal class GithubSearch(private val api: GithubSearchApi, private val strategy: FetchStrategy) {

    suspend fun search(query: String, limit: Int): SearchOutcome {
        val users: Result<List<GithubResult>>
        val repositories: Result<List<GithubResult>>
        coroutineScope {
            val pendingUsers = async { attempt { api.searchUsers(query, strategy.perPage) } }
            val pendingRepositories = async { attempt { api.searchRepositories(query, strategy.perPage) } }
            users = pendingUsers.await()
            repositories = pendingRepositories.await()
        }

        val foundUsers = users.getOrNull()
        val foundRepositories = repositories.getOrNull()
        return when {
            foundUsers != null && foundRepositories != null ->
                SearchOutcome.Complete(merge(foundUsers, foundRepositories, limit))

            foundUsers != null ->
                SearchOutcome.UsersOnly(
                    items = merge(foundUsers, emptyList(), limit),
                    reason = repositories.searchError(),
                )

            foundRepositories != null ->
                SearchOutcome.ReposOnly(
                    items = merge(emptyList(), foundRepositories, limit),
                    reason = users.searchError(),
                )

            else -> throw totalFailure(users, repositories)
        }
    }

    private fun merge(users: List<GithubResult>, repositories: List<GithubResult>, limit: Int): List<GithubResult> =
        (users + repositories).sortedWith(GithubResultComparator).take(limit)
}

// The strategy names the compromise. This module holds the count that the
// compromise costs, because the count is a detail of the transport.
internal val FetchStrategy.perPage: Int
    get() = when (this) {
        FetchStrategy.Overfetch -> MAX_RESULTS
        FetchStrategy.Split -> MAX_RESULTS / 2
    }

private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
    Result.failure(failure)
}

private fun Result<*>.searchError(): SearchError = exceptionOrNull()?.toSearchError() ?: SearchError.Unknown()

// A spent quota is the failure worth naming, so it wins over the other half.
private fun totalFailure(users: Result<*>, repositories: Result<*>): SearchException {
    val cause = users.exceptionOrNull() ?: repositories.exceptionOrNull()
    val errors = listOf(users.searchError(), repositories.searchError())
    val error = errors.firstOrNull { it is SearchError.RateLimited } ?: errors.first()
    return SearchException(error, cause)
}
