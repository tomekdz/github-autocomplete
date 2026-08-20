package com.example.tdziergwa.autocomplete.domain

import com.example.tdziergwa.autocomplete.github.model.GithubResult

sealed interface SearchOutcome {

    val items: List<GithubResult>

    data class Complete(override val items: List<GithubResult>) : SearchOutcome

    data class UsersOnly(override val items: List<GithubResult>, val reason: SearchError) : SearchOutcome

    data class ReposOnly(override val items: List<GithubResult>, val reason: SearchError) : SearchOutcome
}
