package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.network.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ColorCardItem(
    val color: Color,
    var thumbnail: String
)

class ColorViewModel(private val repository: Repository, private val application: Application) : AndroidViewModel(application) {

    private val _colorCardDataset = MutableStateFlow<List<ColorCardItem>>(emptyList())
    val colorCardDataset: StateFlow<List<ColorCardItem>> get() = _colorCardDataset

    private val _liveSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val liveSchedule: StateFlow<List<ScheduleItem>> = _liveSchedule

    fun setLiveSchedule(liveSchedule: List<ScheduleItem>) {
        _liveSchedule.value = liveSchedule
        val dataset = liveSchedule.map { it ->
            ColorCardItem(
                color = Color.Red,
                thumbnail = it.playlist.artwork
            )
        }

        _colorCardDataset.value = dataset

        liveSchedule.forEach {
            extractPaletteForUrl(it.playlist.artwork)
        }
    }

    private val _paletteMap = MutableStateFlow<Map<String, Palette>>(emptyMap())
    val paletteMap: StateFlow<Map<String, Palette>> = _paletteMap

    fun extractPaletteForUrl(url: String) {
        if (_paletteMap.value.containsKey(url)) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val loader = ImageLoader(application.applicationContext)
            val request = ImageRequest.Builder(application.applicationContext)
                .data(url)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            bitmap?.let {
                // Generate a Palette from the Bitmap.
                Palette.from(it).generate { palette ->
                    palette?.let {
                        _colorCardDataset.update { currentList ->
                            currentList.map { item ->
                                if (item.thumbnail == url) {
                                    item.copy(color = Color(palette.getVibrantColor(palette.getMutedColor(Color.Red.toArgb()))))
//                                    item.copy(color = Color(palette.getVibrantColor(Color.Red.toArgb())))
                                } else item
                            }
                        }
                    }
                }
            }
        }
    }

    // Function to get a Palette for a specific URL (or null if not found)
    fun getPaletteForUrl(url: String): Palette? {
        return _paletteMap.value[url]
    }

    private fun getArtworkSwatchFromPalette() {
//        val swatch: Swatch? = if (isDarkTheme.value) {
//            artworkColorPalette.value?.vibrantSwatch
////            artworkColorPalette.value?.darkVibrantSwatch
////                ?: artworkColorPalette.value?.darkMutedSwatch
//        } else {
//            artworkColorPalette.value?.vibrantSwatch
////            artworkColorPalette.value?.lightVibrantSwatch
////                ?: artworkColorPalette.value?.lightMutedSwatch
////                ?: artworkColorPalette.value?.mutedSwatch
////                ?: artworkColorPalette.value?.vibrantSwatch
//        }
//
//        _artworkColorSwatch.value = swatch
    }

    fun loadImageAndExtractPalette(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Load the image using Coil.
            val loader = ImageLoader(application.applicationContext)
            val request = ImageRequest.Builder(application.applicationContext)
                .data(url)
                .allowHardware(false) // Prevent hardware Bitmaps to enable Palette processing
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            bitmap?.let {
                // Generate a Palette from the Bitmap.
                Palette.from(it).generate { palette ->
//                    _artworkColorPalette.value = palette

                    palette?.let {
                        getArtworkSwatchFromPalette()
                    }
                }
            }
        }
    }
}


class ColorViewModelFactory(private val repository: Repository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColorViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
