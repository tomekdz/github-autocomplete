package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val HTTP_UNPROCESSABLE_CONTENT = 422

// GitHub asks for the media type and the version of the API. Without the
// version header the answer follows the newest version, which can change.
private const val ACCEPT_JSON = "Accept: application/vnd.github+json"
private const val API_VERSION = "X-GitHub-Api-Version: 2022-11-28"

internal interface GithubSearchService {

    @Headers(ACCEPT_JSON, API_VERSION)
    @GET("search/users")
    suspend fun searchUsers(@Query("q") query: String, @Query("per_page") perPage: Int): UserSearchResponseDto

    @Headers(ACCEPT_JSON, API_VERSION)
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int,
    ): RepositorySearchResponseDto
}

internal class RetrofitGithubSearchApi(private val service: GithubSearchService) : GithubSearchApi {

    override suspend fun searchUsers(query: String, perPage: Int): List<GithubResult.User> =
        emptyWhenGithubRejectsTheQuery {
            service.searchUsers(query, perPage).items.map(UserDto::toResult)
        }

    override suspend fun searchRepositories(query: String, perPage: Int): List<GithubResult.Repository> =
        emptyWhenGithubRejectsTheQuery {
            service.searchRepositories(query, perPage).items.map(RepositoryDto::toResult)
        }
}

private suspend fun <T> emptyWhenGithubRejectsTheQuery(block: suspend () -> List<T>): List<T> = try {
    block()
} catch (rejected: HttpException) {
    if (rejected.code() == HTTP_UNPROCESSABLE_CONTENT) emptyList() else throw rejected
}
