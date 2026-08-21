package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult
import java.text.Collator
import java.util.Locale

// Collator.compare mutates the instance, so two threads cannot share one.
private val collator = ThreadLocal.withInitial {
    Collator.getInstance(Locale.ROOT).apply { strength = Collator.SECONDARY }
}

internal val GithubResultComparator: Comparator<GithubResult> =
    Comparator<GithubResult> { left, right ->
        collator.get().compare(left.sortKey, right.sortKey)
    }
        .thenBy { it is GithubResult.Repository }
        .thenBy { it.id }
