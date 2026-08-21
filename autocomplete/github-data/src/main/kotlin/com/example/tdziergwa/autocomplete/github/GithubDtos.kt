package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSearchResponseDto(val items: List<UserDto> = emptyList())

@Serializable
internal data class UserDto(
    val id: Long,
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("html_url") val htmlUrl: String,
)

@Serializable
internal data class RepositorySearchResponseDto(val items: List<RepositoryDto> = emptyList())

@Serializable
internal data class RepositoryDto(
    val id: Long,
    val name: String,
    val owner: OwnerDto,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("html_url") val htmlUrl: String,
)

@Serializable
internal data class OwnerDto(val login: String)

internal fun UserDto.toResult() = GithubResult.User(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
)

internal fun RepositoryDto.toResult() = GithubResult.Repository(
    id = id,
    name = name,
    ownerLogin = owner.login,
    stars = stars,
    htmlUrl = htmlUrl,
)
