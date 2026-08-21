package com.example.tdziergwa.autocomplete.github.ui

import com.example.tdziergwa.autocomplete.github.githubTokenInterceptor
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val CALL_TIMEOUT_SECONDS = 15L

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class GithubHttpClient

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GithubAutocompleteModule {

    @BindsOptionalOf
    @GithubToken
    abstract fun optionalGithubToken(): String

    companion object {

        @Provides
        @Singleton
        @GithubHttpClient
        fun githubCallFactory(@GithubToken token: Optional<String>): Call.Factory {
            val usable = token.filter(String::isNotEmpty).orElse(null)
            return OkHttpClient.Builder()
                .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .apply { usable?.let { addInterceptor(githubTokenInterceptor(it)) } }
                .build()
        }
    }
}
