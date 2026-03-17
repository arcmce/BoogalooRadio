package com.arcmce.boogaloo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
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

    private val _cloudcastData = MutableStateFlow<Map<String, MixCloudCloudcast?>>(emptyMap())
    val cloudcastData: StateFlow<Map<String, MixCloudCloudcast?>> = _cloudcastData

    private val fetchedKeys = mutableSetOf<String>()

    private val lastRequestTimes = mutableMapOf<String, Long>()

    private val timeoutMillis = 5000L

    fun fetchPlaylist() {
        viewModelScope.launch {
            try {
                val response = repository.getPlaylist()
                if (response.isSuccessful) {
                    val dataset = response.body()?.data?.map { playlist ->
                        CatchUpCardItem(
                            name = playlist.name,
                            thumbnail = _cloudcastData.value[playlist.slug]?.data?.firstOrNull()?.pictures?.large
                                ?: playlist.owner.pictures.large,
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
        val currentTime = System.currentTimeMillis()

        if (fetchedKeys.contains(key)) {
            Log.d("CatchUpViewModel", "$key request skipped - already fetched")
            return
        }
        if (lastRequestTimes[key]?.let { currentTime - it < timeoutMillis } == true) {
            Log.d("CatchUpViewModel", "$key request skipped - timeout")
            return
        }

        lastRequestTimes[key] = currentTime

        viewModelScope.launch {
            try {
                val response = repository.getCloudcast(key)
                if (response.isSuccessful) {
                    val cloudcast = response.body()
                    _cloudcastData.update { it + (key to cloudcast) }

                    fetchedKeys.add(key)

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
            }
        }
    }

    fun getCloudcast(key: String): MixCloudCloudcast? {
        return _cloudcastData.value[key]
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
