package com.arcmce.boogaloo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.arcmce.boogaloo.network.model.MixCloudPlaylist
import com.arcmce.boogaloo.network.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class CatchUpCardItem(
    val name: String,
    var thumbnail: String,
    val slug: String,
)

class CatchUpViewModel(private val repository: Repository) : ViewModel() {

    private val _catchupCardDataset = MutableStateFlow<List<CatchUpCardItem>>(emptyList())
    val catchupCardDataset: StateFlow<List<CatchUpCardItem>> get() = _catchupCardDataset

    private val fetchedKeys = mutableSetOf<String>()

    fun fetchPlaylist() {
        viewModelScope.launch {
            try {
                val response = repository.getPlaylist()
                if (response.isSuccessful) {
                    val existing = _catchupCardDataset.value.associateBy { it.slug }
                    val dataset = response.body()?.data?.map { playlist ->
                        CatchUpCardItem(
                            name = playlist.name,
                            thumbnail = existing[playlist.slug]?.thumbnail ?: playlist.owner.pictures.large,
                            slug = playlist.slug
                        )
                    } ?: emptyList()

                    _catchupCardDataset.value = dataset
                    Log.d("CatchUpViewModel", "fetchPlaylist success")
                }
            } catch (e: Exception) {
                Log.e("CatchUpViewModel", "fetchPlaylist failed", e)
            }
        }
    }

    fun fetchCloudcastData(key: String) {
        if (fetchedKeys.contains(key)) return
        fetchedKeys.add(key)

        viewModelScope.launch {
            try {
                val response = repository.getCloudcast(key)
                if (response.isSuccessful) {
                    val cloudcast = response.body()
                    _catchupCardDataset.update { currentList ->
                        currentList.map { item ->
                            if (item.slug == key) {
                                item.copy(thumbnail = cloudcast?.data?.firstOrNull()?.pictures?.large ?: item.thumbnail)
                            } else item
                        }
                    }
                    Log.d("CatchUpViewModel", "fetchCloudcastData success $key")
                }
            } catch (e: Exception) {
                Log.e("CatchUpViewModel", "fetchCloudcastData failed $key", e)
                fetchedKeys.remove(key)
            }
        }
    }
}

class CatchUpViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatchUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatchUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
