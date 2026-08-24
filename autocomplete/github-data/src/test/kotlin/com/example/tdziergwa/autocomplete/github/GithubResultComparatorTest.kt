package com.example.tdziergwa.autocomplete.github

import com.example.tdziergwa.autocomplete.github.model.GithubResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GithubResultComparatorTest {

    @Test
    @DisplayName("sorts users and repositories together, ignoring case")
    fun sortsCaseInsensitively() {
        val sorted = listOf(user(1, "zeta"), repo(2, "Alpha"), user(3, "beta"))
            .sortedWith(GithubResultComparator)

        assertEquals(listOf("Alpha", "beta", "zeta"), sorted.map { it.sortKey })
    }

    @Test
    @DisplayName("puts an accented letter next to its base letter, not after z")
    fun sortsAccentedLettersAlphabetically() {
        val sorted = listOf(user(1, "zebra"), repo(2, "angstrom"), user(3, "ängstrom"), repo(4, "Apple"))
            .sortedWith(GithubResultComparator)

        assertEquals(listOf("angstrom", "ängstrom", "Apple", "zebra"), sorted.map { it.sortKey })
    }

    @Test
    @DisplayName("orders digits before letters")
    fun sortsDigitsFirst() {
        val sorted = listOf(user(1, "beta"), repo(2, "2fa"), user(3, "alpha"))
            .sortedWith(GithubResultComparator)

        assertEquals(listOf("2fa", "alpha", "beta"), sorted.map { it.sortKey })
    }

    @Test
    @DisplayName("a name tie puts the user before the repository, then orders by id")
    fun breaksTiesStably() {
        val sorted = listOf(repo(9, "kotlin"), user(4, "kotlin"), user(2, "kotlin"))
            .sortedWith(GithubResultComparator)

        assertEquals(listOf(2L, 4L, 9L), sorted.map { it.id })
    }

    @Test
    @DisplayName("an empty list stays empty")
    fun sortsEmptyList() {
        assertEquals(emptyList<GithubResult>(), emptyList<GithubResult>().sortedWith(GithubResultComparator))
    }
}
