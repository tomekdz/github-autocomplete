package com.example.tdziergwa.feature.home

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import com.example.tdziergwa.autocomplete.github.ui.GithubAutocomplete
import com.example.tdziergwa.core.ui.GithubAutocompleteTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var strategy by rememberSaveable { mutableStateOf(FetchStrategy.Overfetch) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        StrategyChoice(
            strategy = strategy,
            onStrategyChange = { strategy = it },
            modifier = Modifier.padding(vertical = 8.dp),
        )

        GithubAutocomplete(
            modifier = Modifier.fillMaxWidth(),
            strategy = strategy,
            onResultClick = { result -> context.openOnGithub(result.htmlUrl) },
        )
    }
}

private fun Context.openOnGithub(url: String) {
    try {
        CustomTabsIntent.Builder().build().launchUrl(this, url.toUri())
    } catch (ignored: ActivityNotFoundException) {
        // A device with no browser cannot show the page. The screen does not change.
    }
}

@Composable
private fun StrategyChoice(
    strategy: FetchStrategy,
    onStrategyChange: (FetchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FetchStrategy.entries.forEach { choice ->
                FilterChip(
                    selected = choice == strategy,
                    onClick = { onStrategyChange(choice) },
                    label = { Text(stringResource(labelOf(choice))) },
                )
            }
        }

        Text(
            text = stringResource(explanationOf(strategy)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun labelOf(strategy: FetchStrategy) = when (strategy) {
    FetchStrategy.Overfetch -> R.string.home_strategy_overfetch
    FetchStrategy.Split -> R.string.home_strategy_split
}

private fun explanationOf(strategy: FetchStrategy) = when (strategy) {
    FetchStrategy.Overfetch -> R.string.home_strategy_overfetch_explanation
    FetchStrategy.Split -> R.string.home_strategy_split_explanation
}

@Preview(showBackground = true)
@Composable
private fun StrategyChoicePreview() {
    GithubAutocompleteTheme {
        StrategyChoice(strategy = FetchStrategy.Overfetch, onStrategyChange = {})
    }
}
