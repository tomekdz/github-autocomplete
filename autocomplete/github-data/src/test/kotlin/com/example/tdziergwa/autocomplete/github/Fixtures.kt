package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult

internal fun user(id: Long, login: String) = GithubResult.User(id, login, null, "https://github.com/$login")

internal fun repo(id: Long, name: String) =
    GithubResult.Repository(id, name, "owner", 0, "https://github.com/owner/$name")
