package com.example.tdziergwa.autocomplete.github

import okhttp3.Interceptor

/**
 * Puts the token on every request. The search endpoints answer without a token,
 * but the quota is much smaller.
 */
fun githubTokenInterceptor(token: String): Interceptor = Interceptor { chain ->
    chain.proceed(
        chain.request()
            .newBuilder()
            .header("Authorization", "Bearer $token")
            .build(),
    )
}
