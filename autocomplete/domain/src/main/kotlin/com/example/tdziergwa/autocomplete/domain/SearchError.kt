package com.example.tdziergwa.autocomplete.domain

sealed interface SearchError {

    data object Network : SearchError

    /** The token is absent, wrong, or expired. */
    data object Unauthorized : SearchError

    data class RateLimited(val retryAfterSeconds: Long? = null) : SearchError

    data class Unknown(val cause: Throwable? = null) : SearchError
}
