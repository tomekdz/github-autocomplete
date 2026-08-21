package com.example.tdziergwa.autocomplete.github.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdziergwa.autocomplete.domain.AutocompleteState
import com.example.tdziergwa.autocomplete.github.githubSearch
import com.example.tdziergwa.autocomplete.github.model.FetchStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Call
import javax.inject.Inject

@HiltViewModel
internal class GithubAutocompleteViewModel @Inject constructor(@GithubHttpClient callFactory: Call.Factory) :
    ViewModel() {

    private val searches = FetchStrategy.entries.associateWith { githubSearch(callFactory, it) }

    private val strategy = MutableStateFlow(FetchStrategy.Overfetch)

    val state = AutocompleteState(
        search = { query, limit ->
            searches.getValue(strategy.value)(query, limit)
        },
        scope = viewModelScope,
    )

    fun setStrategy(value: FetchStrategy) {
        if (strategy.value == value) return
        strategy.value = value
        state.refresh()
    }
}
