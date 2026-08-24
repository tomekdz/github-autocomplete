package com.example.tdziergwa.di

import com.example.tdziergwa.BuildConfig
import com.example.tdziergwa.autocomplete.github.ui.GithubToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object GithubTokenModule {

    @Provides
    @GithubToken
    fun githubToken(): String = BuildConfig.GITHUB_TOKEN
}
