package com.example.tdziergwa.autocomplete.github.model

/**
 * The compromise that the component makes when it asks the two GitHub searches
 * for candidates.
 *
 * GitHub cannot sort alphabetically, therefore the component sorts a set of
 * candidates that relevance ranked. A larger set gives a better alphabetical
 * result, and costs more bytes.
 */
enum class FetchStrategy {

    /** Each search gives the whole limit. The sort sees twice the limit. */
    Overfetch,

    /** Each search gives half of the limit. The sort discards nothing. */
    Split,
}
