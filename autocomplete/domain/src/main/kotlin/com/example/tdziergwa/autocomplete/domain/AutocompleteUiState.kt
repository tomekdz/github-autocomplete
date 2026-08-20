package com.example.tdziergwa.autocomplete.domain

import com.example.tdziergwa.autocomplete.github.model.GithubResult

/**
 * [Idle] means the query is shorter than [AutocompleteConfig.minQueryLength].
 *
 * [UsersOnly] and [ReposOnly] are partial successes. [UsersOnly] means the
 * repository search failed, and [ReposOnly] means the user search failed. Both
 * can hold an empty list, because the part that worked can still match nothing.
 */
sealed interface AutocompleteUiState {

    data object Idle : AutocompleteUiState

    data object Loading : AutocompleteUiState

    data object Empty : AutocompleteUiState

    data class Success(val items: List<GithubResult>) : AutocompleteUiState

    data class UsersOnly(val items: List<GithubResult>, val reason: SearchError) : AutocompleteUiState

    data class ReposOnly(val items: List<GithubResult>, val reason: SearchError) : AutocompleteUiState

    data class Failed(val reason: SearchError) : AutocompleteUiState
}
