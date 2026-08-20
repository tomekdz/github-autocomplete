package com.example.tdziergwa.autocomplete.domain

/**
 * A source throws this when the whole search fails and it already knows the
 * category. A source that throws anything else gets [SearchError.Unknown].
 */
class SearchException(val error: SearchError, cause: Throwable? = null) : Exception(cause)
