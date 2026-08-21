package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.domain.SearchException
import retrofit2.HttpException
import java.io.IOException

private const val MILLIS_IN_SECOND = 1000
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_TOO_MANY_REQUESTS = 429

internal fun Throwable.toSearchError(): SearchError = when (this) {
    is SearchException -> error

    is HttpException if code() == HTTP_UNAUTHORIZED -> SearchError.Unauthorized

    is HttpException if code() in setOf(HTTP_FORBIDDEN, HTTP_TOO_MANY_REQUESTS) ->
        SearchError.RateLimited(retryAfterSeconds())

    is IOException -> SearchError.Network

    else -> SearchError.Unknown(this)
}

// GitHub names the wait in either Retry-After or the reset timestamp.
private fun HttpException.retryAfterSeconds(): Long? {
    val headers = response()?.headers() ?: return null
    headers["Retry-After"]?.toLongOrNull()?.let { return it }
    val reset = headers["X-RateLimit-Reset"]?.toLongOrNull() ?: return null
    return (reset - System.currentTimeMillis() / MILLIS_IN_SECOND).coerceAtLeast(0)
}
