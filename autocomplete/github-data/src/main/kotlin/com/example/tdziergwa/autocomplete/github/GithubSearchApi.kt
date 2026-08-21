package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult

/**
 * A query that GitHub rejects as invalid search syntax (HTTP 422) returns an
 * empty list. Only a transport or quota failure throws.
 */
internal interface GithubSearchApi {

    suspend fun searchUsers(query: String, perPage: Int): List<GithubResult.User>

    suspend fun searchRepositories(query: String, perPage: Int): List<GithubResult.Repository>
}
