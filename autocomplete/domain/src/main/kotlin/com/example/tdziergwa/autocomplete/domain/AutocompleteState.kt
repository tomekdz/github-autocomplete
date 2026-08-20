package com.example.tdziergwa.autocomplete.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Holds the query and the state that the query produces.
 *
 * The state holder is not tied to a platform. A caller supplies the scope, and
 * the scope decides how long the search lives. A new character cancels the
 * search that is in progress, so only the newest query reaches the search
 * function.
 *
 * @param search finds the results for one query. The state holder gives a
 *   trimmed query that is never shorter than
 *   [AutocompleteConfig.minQueryLength], and gives
 *   [AutocompleteConfig.resultLimit] as the limit. The function must return no
 *   more items than the limit. It throws [SearchException] when the whole
 *   search fails and the category is known, and it lets a
 *   `CancellationException` pass. Any other exception becomes
 *   [SearchError.Unknown].
 * @param scope usually the scope of a `ViewModel`.
 */
class AutocompleteState(
    private val search: suspend (query: String, limit: Int) -> SearchOutcome,
    scope: CoroutineScope,
    private val config: AutocompleteConfig = AutocompleteConfig(),
) {

    private val typed = MutableStateFlow("")

    private val reload = MutableStateFlow(0)

    val query: StateFlow<String> = typed.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<AutocompleteUiState> = combine(
        typed.map(String::trim).distinctUntilChanged(),
        reload,
    ) { query, _ -> query }
        .debounce(config.debounce)
        .flatMapLatest(::stateFor)
        .stateIn(scope, SharingStarted.Lazily, AutocompleteUiState.Idle)

    fun onQueryChange(value: String) {
        typed.value = value
    }

    fun refresh() {
        reload.value += 1
    }

    private fun stateFor(query: String): Flow<AutocompleteUiState> = if (query.length < config.minQueryLength) {
        flowOf(AutocompleteUiState.Idle)
    } else {
        flow { emit(search(query)) }
            .onStart { emit(AutocompleteUiState.Loading) }
            .catch { emit(AutocompleteUiState.Failed(it.toSearchError())) }
    }

    private suspend fun search(query: String): AutocompleteUiState =
        when (val outcome = search(query, config.resultLimit)) {
            is SearchOutcome.Complete ->
                if (outcome.items.isEmpty()) {
                    AutocompleteUiState.Empty
                } else {
                    AutocompleteUiState.Success(outcome.items)
                }

            is SearchOutcome.UsersOnly -> AutocompleteUiState.UsersOnly(outcome.items, outcome.reason)

            is SearchOutcome.ReposOnly -> AutocompleteUiState.ReposOnly(outcome.items, outcome.reason)
        }
}

private fun Throwable.toSearchError(): SearchError = if (this is SearchException) error else SearchError.Unknown(this)
