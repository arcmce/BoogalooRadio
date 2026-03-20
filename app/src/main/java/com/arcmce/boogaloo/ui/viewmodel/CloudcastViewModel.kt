package com.arcmce.boogaloo.ui.viewmodel

import android.util.Log
import com.arcmce.boogaloo.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arcmce.boogaloo.network.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class CloudcastCardItem(
    val name: String,
    var thumbnail: String,
    val url: String,
)

class CloudcastViewModel(private val repository: Repository) : ViewModel() {

    init {
        if (BuildConfig.DEBUG) Log.d("CloudcastViewModel", "NEW INSTANCE created: ${System.identityHashCode(this)}")
    }

    private val _cloudcastCardDataset = MutableStateFlow<List<CloudcastCardItem>>(emptyList())
    val cloudcastCardDataset: StateFlow<List<CloudcastCardItem>> get() = _cloudcastCardDataset

    private val _readyForSlug = MutableStateFlow<String?>(null)
    val readyForSlug: StateFlow<String?> = _readyForSlug

    fun loadCloudcast(slug: String) {
        if (BuildConfig.DEBUG) Log.d("CloudcastViewModel", "loadCloudcast called: slug=$slug, instance=${System.identityHashCode(this)}, readyForSlug was=${_readyForSlug.value}")
        _readyForSlug.value = null
        _cloudcastCardDataset.value = emptyList()
        viewModelScope.launch {
            if (BuildConfig.DEBUG) Log.d("CloudcastViewModel", "fetching slug=$slug")
            try {
                val response = repository.getCloudcast(slug)
                if (response.isSuccessful) {
                    _cloudcastCardDataset.value = response.body()?.data
                        ?.sortedByDescending { it.createdTime }
                        ?.map { item ->
                            CloudcastCardItem(
                                name = item.name,
                                thumbnail = item.pictures.large,
                                url = item.url
                            )
                        } ?: emptyList()
                    if (BuildConfig.DEBUG) Log.d("CloudcastViewModel", "setting readyForSlug=$slug")
                    _readyForSlug.value = slug
                    if (BuildConfig.DEBUG) Log.d("CloudcastViewModel", "loadCloudcast success $slug")
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("CloudcastViewModel", "loadCloudcast failed $slug", e)
            }
        }
    }
}

class CloudcastViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudcastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CloudcastViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
