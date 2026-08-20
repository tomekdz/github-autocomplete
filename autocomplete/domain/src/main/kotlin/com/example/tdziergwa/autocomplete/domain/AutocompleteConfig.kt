package com.example.tdziergwa.autocomplete.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class AutocompleteConfig(
    val minQueryLength: Int = DEFAULT_MIN_QUERY_LENGTH,
    val debounce: Duration = DEFAULT_DEBOUNCE,
    val resultLimit: Int = MAX_RESULTS,
) {
    init {
        require(minQueryLength >= 1) { "minQueryLength must be 1 or more" }
        require(resultLimit >= 1) { "resultLimit must be 1 or more" }
    }

    companion object {
        const val DEFAULT_MIN_QUERY_LENGTH = 3
        const val MAX_RESULTS = 50
        val DEFAULT_DEBOUNCE = 300.milliseconds
    }
}
