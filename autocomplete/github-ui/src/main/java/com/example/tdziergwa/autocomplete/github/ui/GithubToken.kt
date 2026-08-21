package com.example.tdziergwa.autocomplete.github.ui

import javax.inject.Qualifier

/**
 * Marks the GitHub token that the host application supplies.
 *
 * The binding is optional. A host that has no token binds nothing, and the
 * component searches without a token, which GitHub permits at a lower rate. An
 * empty string has the same result as no binding.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GithubToken
