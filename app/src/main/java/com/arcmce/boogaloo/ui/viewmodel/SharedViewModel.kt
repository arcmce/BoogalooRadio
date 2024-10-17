package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setIsDarkTheme(isDarkTheme: Boolean) {
        _isDarkTheme.value = isDarkTheme
        getArtworkSwatchFromPalette()
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
        loadImageAndExtractPalette()
    }

    private val _artworkColorPalette = MutableStateFlow<Palette?>(null)
    val artworkColorPalette: StateFlow<Palette?> = _artworkColorPalette

    private val _artworkColorSwatch = MutableStateFlow<Swatch?>(null)
    val artworkColorSwatch: StateFlow<Swatch?> = _artworkColorSwatch

    private val _cloudcast = MutableStateFlow<MixCloudCloudcast?>(null)
    val cloudcast: StateFlow<MixCloudCloudcast?> = _cloudcast

    // Function to set CloudcastData
    fun setCloudcast(data: MixCloudCloudcast?) {
        _cloudcast.value = data
    }

    fun getCloudcast(): MixCloudCloudcast? {
        return _cloudcast.value
    }

    private fun getArtworkSwatchFromPalette() {
        val swatch: Swatch? = if (isDarkTheme.value) {
            artworkColorPalette.value?.darkVibrantSwatch ?: artworkColorPalette.value?.darkMutedSwatch
        } else {
            artworkColorPalette.value?.lightVibrantSwatch
                ?: artworkColorPalette.value?.lightMutedSwatch
                ?: artworkColorPalette.value?.mutedSwatch
                ?: artworkColorPalette.value?.vibrantSwatch
        }

        _artworkColorSwatch.value = swatch
    }

    fun loadImageAndExtractPalette() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load the image using Coil.
            val loader = ImageLoader(application.applicationContext)
            val request = ImageRequest.Builder(application.applicationContext)
                .data(artworkUrl.value)
                .allowHardware(false) // Prevent hardware Bitmaps to enable Palette processing
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            bitmap?.let {
                // Generate a Palette from the Bitmap.
                Palette.from(it).generate { palette ->
                    _artworkColorPalette.value = palette

                    palette?.let {
                        getArtworkSwatchFromPalette()
                    }
                }
            }
        }
    }
}


class SharedViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
