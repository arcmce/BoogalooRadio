package com.arcmce.boogaloo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    private val _liveTitle = MutableLiveData<String?>()
    val liveTitle: LiveData<String?> get() = _liveTitle

    fun setLiveTitle(data: String?) {
        _liveTitle.value = data
    }

    // Use StateFlow for data exposure (you can also use LiveData if preferred)
    private val _cloudcast = MutableStateFlow<MixCloudCloudcast?>(null)
    val cloudcast: StateFlow<MixCloudCloudcast?> = _cloudcast

    // Function to set CloudcastData
    fun setCloudcast(data: MixCloudCloudcast?) {
        _cloudcast.value = data
    }

    // Optional: Add getter if you want to retrieve specific fields
    fun getCloudcast(): MixCloudCloudcast? {
        return _cloudcast.value
    }
}