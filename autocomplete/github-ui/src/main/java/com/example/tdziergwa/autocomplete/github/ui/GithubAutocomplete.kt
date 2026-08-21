package com.example.tdziergwa.autocomplete.github.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import com.example.tdziergwa.autocomplete.github.model.GithubResult
import com.example.tdziergwa.autocomplete.ui.AutocompleteField

/**
 * A GitHub autocomplete that a screen can use with one line.
 *
 * The component finds its own dependencies. The host application binds the
 * optional token with [GithubToken], and binds nothing else.
 *
 * @param strategy how many items each endpoint gives. See `FetchStrategy`.
 * @param onResultClick receives the result that the user selects.
 */
@Composable
fun GithubAutocomplete(
    modifier: Modifier = Modifier,
    strategy: FetchStrategy = FetchStrategy.Overfetch,
    onResultClick: (GithubResult) -> Unit = {},
) {
    val viewModel: GithubAutocompleteViewModel = hiltViewModel()
    LaunchedEffect(strategy) { viewModel.setStrategy(strategy) }

    AutocompleteField(
        state = viewModel.state,
        itemContent = { result -> GithubResultRow(result, onClick = { onResultClick(result) }) },
        modifier = modifier,
        placeholder = stringResource(R.string.github_autocomplete_placeholder),
    )
}
