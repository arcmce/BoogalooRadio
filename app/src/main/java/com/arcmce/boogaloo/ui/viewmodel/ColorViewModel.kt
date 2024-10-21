package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.RadioSchedule
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.network.repository.Repository
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.util.AppConstants
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneId

class ColorViewModel(private val repository: Repository, private val application: Application) : AndroidViewModel(application) {

    private val _liveSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val liveSchedule: StateFlow<List<ScheduleItem>> = _liveSchedule

    fun setLiveSchedule(liveSchedule: List<ScheduleItem>) {
        _liveSchedule.value = liveSchedule
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
