package com.example.tdziergwa.autocomplete.github.model

/**
 * One suggestion of the GitHub source.
 *
 * [sortKey] is the name that the alphabetical order uses: the login of a user,
 * and the name of a repository.
 */
sealed interface GithubResult {
    val id: Long
    val sortKey: String
    val htmlUrl: String

    data class User(override val id: Long, val login: String, val avatarUrl: String?, override val htmlUrl: String) :
        GithubResult {
        override val sortKey: String get() = login
    }

    data class Repository(
        override val id: Long,
        val name: String,
        val ownerLogin: String,
        val stars: Int,
        override val htmlUrl: String,
    ) : GithubResult {
        override val sortKey: String get() = name
    }
}
