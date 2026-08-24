package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.SearchError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

private const val QUOTA_MESSAGE = """{ "message": "API rate limit exceeded" }"""

private fun httpFailure(code: Int, vararg headers: Pair<String, String>) = HttpException(
    Response.error<Unit>(
        QUOTA_MESSAGE.toResponseBody("application/json".toMediaType()),
        okhttp3.Response.Builder()
            .code(code)
            .message("failed")
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .request(okhttp3.Request.Builder().url("https://api.github.com/search/users").build())
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .build(),
    ),
)

class SearchErrorsTest {

    @Test
    @DisplayName("a token that GitHub rejects is its own state, and not an unknown failure")
    fun mapsARejectedToken() {
        assertEquals(SearchError.Unauthorized, httpFailure(401).toSearchError())
    }

    @Test
    @DisplayName("a spent quota takes the wait from Retry-After")
    fun readsRetryAfter() {
        val error = httpFailure(403, "Retry-After" to "30").toSearchError()

        assertEquals(SearchError.RateLimited(30), error)
    }

    @Test
    @DisplayName("a spent quota takes the wait from the reset time when Retry-After is absent")
    fun readsTheResetTime() {
        val reset = System.currentTimeMillis() / 1000 + 60

        val error = httpFailure(403, "X-RateLimit-Reset" to reset.toString()).toSearchError()

        val seconds = (error as SearchError.RateLimited).retryAfterSeconds
        assertTrue(seconds in 55L..60L, "the wait was $seconds seconds")
    }

    @Test
    @DisplayName("a spent quota with no header still names the quota")
    fun reportsAQuotaWithNoHeader() {
        assertEquals(SearchError.RateLimited(null), httpFailure(429).toSearchError())
    }

    @Test
    @DisplayName("a fault of the server is unknown, and not a spent quota")
    fun mapsAServerFault() {
        assertInstanceOf(SearchError.Unknown::class.java, httpFailure(500).toSearchError())
    }

    @Test
    @DisplayName("a network fault is its own state")
    fun mapsANetworkFault() {
        assertEquals(SearchError.Network, SocketTimeoutException("timeout").toSearchError())
    }
}
