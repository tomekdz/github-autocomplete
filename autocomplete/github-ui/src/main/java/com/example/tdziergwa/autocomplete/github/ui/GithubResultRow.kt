package com.example.tdziergwa.autocomplete.github.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tdziergwa.autocomplete.github.model.GithubResult

private val AVATAR_SIZE = 40.dp

@Composable
internal fun GithubResultRow(result: GithubResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(result.sortKey) },
        supportingContent = { Text(supportingTextOf(result)) },
        leadingContent = { Avatar(result) },
        modifier = modifier.clickable(
            onClickLabel = stringResource(R.string.github_result_open),
            onClick = onClick,
        ),
    )
}

@Composable
private fun Avatar(result: GithubResult) {
    when (result) {
        is GithubResult.User -> AsyncImage(
            model = result.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape),
        )

        is GithubResult.Repository -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(result.name.take(1).uppercase())
        }
    }
}

@Composable
private fun supportingTextOf(result: GithubResult): String = when (result) {
    is GithubResult.User -> stringResource(R.string.github_result_user)

    is GithubResult.Repository -> pluralStringResource(
        R.plurals.github_result_repository,
        result.stars,
        result.ownerLogin,
        result.stars,
    )
}
