package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.SearchOutcome
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val GITHUB_API_BASE_URL = "https://api.github.com/"

/**
 * The only way to build the GitHub search. The module holds no dependency
 * injection framework, so the caller supplies the collaborators.
 *
 * @param callFactory the OkHttp client. A caller that has a token adds
 *   [githubTokenInterceptor] to that client.
 * @param baseUrl must end with a slash.
 */
fun githubSearch(
    callFactory: Call.Factory,
    strategy: FetchStrategy = FetchStrategy.Overfetch,
    baseUrl: String = GITHUB_API_BASE_URL,
): suspend (query: String, limit: Int) -> SearchOutcome = GithubSearch(
    api = RetrofitGithubSearchApi(githubSearchService(callFactory, baseUrl)),
    strategy = strategy,
).let { search -> search::search }

internal fun githubSearchService(callFactory: Call.Factory, baseUrl: String): GithubSearchService = Retrofit.Builder()
    .baseUrl(baseUrl)
    .callFactory(callFactory)
    .addConverterFactory(githubJson.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(GithubSearchService::class.java)

private val githubJson = Json { ignoreUnknownKeys = true }
