package com.arcmce.boogaloo.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State

class SharedViewModel : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setIsDarkTheme(isDarkTheme: Boolean) {
        _isDarkTheme.value = isDarkTheme
    }

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

    private val _artworkUrl = MutableStateFlow<String?>(null)
    val artworkUrl: StateFlow<String?> = _artworkUrl

    fun setArtworkUrl(url: String?) {
        _artworkUrl.value = url
    }

    private val _artworkColor = MutableStateFlow(Color.Gray)
    val artworkColor: StateFlow<Color> = _artworkColor

    fun setArtworkColor(color: Color) {
        _artworkColor.value = color
    }

    // Use StateFlow for data exposure
    private val _cloudcast = MutableStateFlow<MixCloudCloudcast?>(null)
    val cloudcast: StateFlow<MixCloudCloudcast?> = _cloudcast

    // Function to set CloudcastData
    fun setCloudcast(data: MixCloudCloudcast?) {
        _cloudcast.value = data
    }

    fun getCloudcast(): MixCloudCloudcast? {
        return _cloudcast.value
    }
}