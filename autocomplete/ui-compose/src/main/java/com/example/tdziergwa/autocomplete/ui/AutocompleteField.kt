package com.example.tdziergwa.autocomplete.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tdziergwa.autocomplete.domain.AutocompleteState
import com.example.tdziergwa.autocomplete.domain.AutocompleteUiState
import com.example.tdziergwa.autocomplete.domain.SearchError
import com.example.tdziergwa.autocomplete.github.model.GithubResult

private val MIN_TOUCH_TARGET = 48.dp

/**
 * A text field with the suggestions that the query finds.
 *
 * The component holds the shared GitHub autocomplete chrome. The caller
 * supplies the state holder, and supplies the layout of one result.
 *
 * @param itemContent the layout of one suggestion. A caller that wants a click
 *   makes this content clickable.
 */
@Composable
fun AutocompleteField(
    state: AutocompleteState,
    itemContent: @Composable (GithubResult) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    val query by state.query.collectAsStateWithLifecycle()
    val uiState by state.uiState.collectAsStateWithLifecycle()

    Column(modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = state::onQueryChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState is AutocompleteUiState.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Status(uiState)
        Warning(uiState)
        Suggestions(uiState.items, itemContent)
    }
}

@Composable
private fun Status(uiState: AutocompleteUiState) {
    val text = when (uiState) {
        AutocompleteUiState.Idle -> ""
        AutocompleteUiState.Loading -> stringResource(R.string.autocomplete_searching)
        AutocompleteUiState.Empty -> stringResource(R.string.autocomplete_no_results)
        is AutocompleteUiState.Success -> resultCount(uiState.items.size)
        is AutocompleteUiState.UsersOnly -> resultCount(uiState.items.size)
        is AutocompleteUiState.ReposOnly -> resultCount(uiState.items.size)
        is AutocompleteUiState.Failed -> messageOf(uiState.reason)
    }
    if (text.isEmpty()) return

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (uiState is AutocompleteUiState.Failed) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun Warning(uiState: AutocompleteUiState) {
    val (missing, reason) = when (uiState) {
        is AutocompleteUiState.UsersOnly -> R.string.autocomplete_repositories_missing to uiState.reason
        is AutocompleteUiState.ReposOnly -> R.string.autocomplete_users_missing to uiState.reason
        else -> return
    }

    Text(
        text = "${stringResource(missing)} ${messageOf(reason)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun Suggestions(items: List<GithubResult>, itemContent: @Composable (GithubResult) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items) { item ->
            Box(modifier = Modifier.heightIn(min = MIN_TOUCH_TARGET)) {
                itemContent(item)
            }
        }
    }
}

@Composable
private fun resultCount(count: Int): String = pluralStringResource(R.plurals.autocomplete_result_count, count, count)

@Composable
private fun messageOf(reason: SearchError): String = when (reason) {
    SearchError.Network -> stringResource(R.string.autocomplete_error_network)

    SearchError.Unauthorized -> stringResource(R.string.autocomplete_error_unauthorized)

    is SearchError.RateLimited ->
        reason.retryAfterSeconds
            ?.let { pluralStringResource(R.plurals.autocomplete_error_quota_seconds, it.toInt(), it) }
            ?: stringResource(R.string.autocomplete_error_quota)

    is SearchError.Unknown -> stringResource(R.string.autocomplete_error_unknown)
}

private val AutocompleteUiState.items: List<GithubResult>
    get() = when (this) {
        is AutocompleteUiState.Success -> items
        is AutocompleteUiState.UsersOnly -> items
        is AutocompleteUiState.ReposOnly -> items
        AutocompleteUiState.Idle, AutocompleteUiState.Loading, AutocompleteUiState.Empty -> emptyList()
        is AutocompleteUiState.Failed -> emptyList()
    }
