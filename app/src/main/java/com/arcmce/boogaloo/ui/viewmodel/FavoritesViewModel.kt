package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arcmce.boogaloo.data.model.FavoriteArtist
import com.arcmce.boogaloo.data.model.FavoriteMix
import com.arcmce.boogaloo.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    application: Application,
    private val repository: FavoritesRepository
) : AndroidViewModel(application) {

    val favoriteArtists: StateFlow<List<FavoriteArtist>> = repository.favoriteArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteMixes: StateFlow<List<FavoriteMix>> = repository.favoriteMixes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteArtistNames: StateFlow<Set<String>> = repository.favoriteArtists
        .map { list -> list.map { it.name }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val favoriteMixUrls: StateFlow<Set<String>> = repository.favoriteMixes
        .map { list -> list.map { it.url }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleArtist(artist: FavoriteArtist) {
        viewModelScope.launch { repository.toggleArtist(artist) }
    }

    fun toggleMix(mix: FavoriteMix) {
        viewModelScope.launch { repository.toggleMix(mix) }
    }
}

class FavoritesViewModelFactory(
    private val application: Application,
    private val repository: FavoritesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
