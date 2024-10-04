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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


data class CatchUpCardItem(
    val name: String,
    var thumbnail: String,
    val slug: String,
//    val url: ArrayList<String>
)

class CatchUpViewModel(private val repository: Repository) : ViewModel() {

    private val _catchupCardDataset = MutableStateFlow<List<CatchUpCardItem>>(emptyList())
    val catchupCardDataset: StateFlow<List<CatchUpCardItem>> get() = _catchupCardDataset

    private val _cloudcastData = MutableStateFlow<Map<String, MixCloudCloudcast?>>(emptyMap())
    val cloudcastData: StateFlow<Map<String, MixCloudCloudcast?>> = _cloudcastData

    private val fetchedKeys = mutableSetOf<String>()

    private val lastRequestTimes = mutableMapOf<String, Long>()

    private val timeoutMillis = 5000L // 5 seconds timeout

    fun fetchPlaylist() {
        viewModelScope.launch {
            val call = repository.getPlaylist()
            call.enqueue(object : Callback<MixCloudPlaylist> {
                override fun onResponse(call: Call<MixCloudPlaylist>, response: Response<MixCloudPlaylist>) {

                    if (response.isSuccessful) {
                        val dataset = response.body()?.data?.map { playlist ->
                            CatchUpCardItem(
                                name = playlist.name,
                                thumbnail = _cloudcastData.value[playlist.slug]?.data?.first()?.pictures?.large
                                    ?: playlist.owner.pictures.large,
                                slug = playlist.slug
                            )
                        } ?: emptyList()

                        _catchupCardDataset.value = dataset
                        Log.d("CatchUpViewModel", "fetchPlaylist success")
                    }
                }

                override fun onFailure(call: Call<MixCloudPlaylist>, t: Throwable) {
//                    _artworkUrl.value = null
                    Log.d("CatchUpViewModel", "fetchPlaylist fail")
                }
            })
        }
    }

    fun fetchCloudcastData(key: String) {

        val currentTime = System.currentTimeMillis()

        // Check if the key is already fetched or the last request was within the timeout period
        if (fetchedKeys.contains(key)) {
            Log.d("CatchUpViewModel", "$key request skipped - already fetched")
            return
        }
        // Check if the key is already fetched or the last request was within the timeout period
        if ((lastRequestTimes[key]?.let { currentTime - it < timeoutMillis } == true)) {
            Log.d("CatchUpViewModel", "$key request skipped - timeout")
            return
        }

        // Update the last request time
        lastRequestTimes[key] = currentTime

        viewModelScope.launch {
            val call = repository.getCloudcast(key)
            call.enqueue(object : Callback<MixCloudCloudcast> {
                override fun onResponse(call: Call<MixCloudCloudcast>, response: Response<MixCloudCloudcast>) {
                    if (response.isSuccessful) {
                        val cloudcast = response.body()
                        _cloudcastData.update { it + (key to cloudcast) }

                        fetchedKeys.add(key)

                        _catchupCardDataset.update { currentList ->
                            currentList.map { item ->
                                if (item.slug == key) {
                                    item.copy(thumbnail = cloudcast?.data?.first()?.pictures?.large ?: item.thumbnail)
                                } else item
                            }
                        }

//                        val updatedList = _catchupCardDataset.value?.map { item ->
//                            if (item.slug == key) {
//                                item.copy(thumbnail = cloudcast?.data?.first()?.pictures?.large ?: item.thumbnail)
//                            } else item
//                        } ?: emptyList()
//                        _catchupCardDataset.value = updatedList

                        Log.d("CatchUpViewModel", "fetchCloudcastData success $key")
                    }
                }

                override fun onFailure(call: Call<MixCloudCloudcast>, t: Throwable) {

                    Log.d("CatchUpViewModel", "fetchCloudcastData fail")
                }
            })
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
