package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.domain.AutocompleteConfig.Companion.MAX_RESULTS
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FetchStrategyTest {

    @Test
    @DisplayName("Overfetch asks each endpoint for the whole limit, so half is discarded")
    fun overfetchAsksForTheWholeLimit() {
        assertEquals(MAX_RESULTS, FetchStrategy.Overfetch.perPage)
    }

    @Test
    @DisplayName("the two Split pages together reach the limit, and no more")
    fun splitPagesReachTheLimit() {
        assertEquals(MAX_RESULTS, FetchStrategy.Split.perPage * 2)
    }
}
