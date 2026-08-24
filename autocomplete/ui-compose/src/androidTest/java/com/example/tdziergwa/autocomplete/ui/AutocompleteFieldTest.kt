package com.example.tdziergwa.autocomplete.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tdziergwa.autocomplete.domain.AutocompleteConfig
import com.example.tdziergwa.autocomplete.domain.AutocompleteState
import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.domain.SearchException
import com.example.tdziergwa.autocomplete.domain.SearchOutcome
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import kotlinx.coroutines.awaitCancellation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds

private const val QUERY = "abc"
private val TEST_DEBOUNCE = 50.milliseconds
private const val WAIT_MILLIS = 5_000L
private val ALPHA = GithubResult.User(1, "alpha", null, "https://github.com/alpha")
private val BETA = GithubResult.Repository(2, "beta", "owner", 42, "https://github.com/owner/beta")

@RunWith(AndroidJUnit4::class)
class AutocompleteFieldTest {

    @get:Rule
    val compose = createComposeRule()

    private val clicked = mutableListOf<GithubResult>()

    private fun start(search: suspend (String, Int) -> SearchOutcome) {
        compose.setContent {
            val scope = rememberCoroutineScope()
            val state = remember {
                AutocompleteState(search, scope, AutocompleteConfig(debounce = TEST_DEBOUNCE))
            }
            MaterialTheme {
                AutocompleteField(
                    state = state,
                    itemContent = { item ->
                        Text(text = item.sortKey, modifier = Modifier.clickable { clicked += item })
                    },
                )
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput(QUERY)
    }

    private fun awaitText(text: String) = compose.waitUntil(WAIT_MILLIS) {
        compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun showsTheLoadingState() {
        start { _, _ -> awaitCancellation() }

        awaitText(string(R.string.autocomplete_searching))
    }

    @Test
    fun showsTheEmptyState() {
        start { _, _ -> SearchOutcome.Complete(emptyList()) }

        awaitText(string(R.string.autocomplete_no_results))
    }

    @Test
    fun showsTheErrorState() {
        start { _, _ -> throw SearchException(SearchError.Network) }

        awaitText(string(R.string.autocomplete_error_network))
    }

    @Test
    fun refreshRunsTheSearchAgain() {
        val answers = ArrayDeque(listOf(listOf(ALPHA), listOf(BETA)))
        lateinit var holder: AutocompleteState

        compose.setContent {
            val scope = rememberCoroutineScope()
            val state = remember {
                AutocompleteState(
                    { _, _ -> SearchOutcome.Complete(answers.removeFirstOrNull() ?: emptyList()) },
                    scope,
                    AutocompleteConfig(debounce = TEST_DEBOUNCE),
                )
            }
            holder = state
            MaterialTheme {
                AutocompleteField(state = state, itemContent = { Text(it.sortKey) })
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput(QUERY)
        awaitText("alpha")

        compose.runOnIdle { holder.refresh() }

        awaitText("beta")
    }

    @Test
    fun reportsAClickOnAnItem() {
        start { _, _ -> SearchOutcome.Complete(listOf(ALPHA, BETA)) }
        awaitText("alpha")

        compose.onNodeWithText("alpha").performClick()

        assertEquals(listOf(ALPHA), clicked)
    }
}
