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

class SharedViewModel(private val repository: Repository, private val application: Application) : AndroidViewModel(application) {

    private var player: Player? = null

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

    private val _liveSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val liveSchedule: StateFlow<List<ScheduleItem>> = _liveSchedule

    private val _liveTitle = MutableStateFlow<String?>(AppConstants.RADIO_TITLE)
    val liveTitle: StateFlow<String?> = _liveTitle

    fun setLiveTitle(data: String?) {
        _liveTitle.value = data
    }

    private val _liveArtist = MutableStateFlow<String?>(AppConstants.DEFAULT_ARTIST)
    val liveArtist: StateFlow<String?> = _liveArtist

//    fun setLiveArtist(data: String?) {
//        _liveArtist.value = data
//    }

    private val _liveArtworkUrl = MutableStateFlow<String?>(null)
    val liveArtworkUrl: StateFlow<String?> = _liveArtworkUrl

    fun setLiveArtworkUrl(url: String?) {
        _liveArtworkUrl.value = url
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

    init {
        setupPlayer()
    }

    private fun getArtworkSwatchFromPalette() {
        val swatch: Swatch? = if (isDarkTheme.value) {
            artworkColorPalette.value?.vibrantSwatch
//            artworkColorPalette.value?.darkVibrantSwatch
//                ?: artworkColorPalette.value?.darkMutedSwatch
        } else {
            artworkColorPalette.value?.vibrantSwatch
//            artworkColorPalette.value?.lightVibrantSwatch
//                ?: artworkColorPalette.value?.lightMutedSwatch
//                ?: artworkColorPalette.value?.mutedSwatch
//                ?: artworkColorPalette.value?.vibrantSwatch
        }

        _artworkColorSwatch.value = swatch
    }

    fun loadImageAndExtractPalette() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load the image using Coil.
            val loader = ImageLoader(application.applicationContext)
            val request = ImageRequest.Builder(application.applicationContext)
                .data(liveArtworkUrl.value)
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

    fun setupPlayer() {
        if (player == null) {

            val sessionToken = SessionToken(
                application.applicationContext,
                ComponentName(application.applicationContext, PlaybackService::class.java)
            )
            val controllerFuture = MediaController.Builder(application.applicationContext, sessionToken).buildAsync()

            controllerFuture.addListener(
                {
                    player = controllerFuture.get()
                },
                MoreExecutors.directExecutor()
            )
        }
    }

    fun updateMetadata(metadataTitle: String, metadataArtist: String, metadataArtworkUri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setUri(AppConstants.RADIO_STREAM_URL)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(metadataTitle)
                    .setArtist(metadataArtist)
                    .setArtworkUri(metadataArtworkUri)
                    .build()
            )
            .build()

        // Update the player with the new media item
        player?.replaceMediaItem(0, mediaItem)
    }

    fun getOnAirItem() {
        val currentDateTime = LocalDateTime.now()

        val onAirItem = liveSchedule.value.find {
            val startDateTime =
                it.start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val endDateTime = it.end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            startDateTime.isBefore(currentDateTime) && endDateTime.isAfter(currentDateTime)
        }

//        liveSchedule.value?.let {
//
//        }
//        var onAirItem: ScheduleItem? = null
//        if (liveSchedule.value.isNotEmpty()) {
//            onAirItem = liveSchedule.value[31]
//        } else {
//            onAirItem = null
//        }

        onAirItem?.let {

            // this is intentionally set as title=artist, artist=title due to response format
            val title = it.playlist.artist
            val artist = it.playlist.title
            val artworkUrl = it.playlist.artwork.replace(".100.", ".600.")

            _liveTitle.value = title
            _liveArtist.value = artist
            _liveArtworkUrl.value = artworkUrl

            viewModelScope.launch(Dispatchers.Main) {
                updateMetadata(title, artist, Uri.parse(artworkUrl))
            }
        }
    }

    fun fetchRadioSchedule() {
        viewModelScope.launch {
            val call = repository.getRadioSchedule()
            call.enqueue(object : Callback<RadioSchedule> {
                override fun onResponse(call: Call<RadioSchedule>, response: Response<RadioSchedule>) {
                    if (response.isSuccessful) {
                        response.body()?.data?.let {
                            _liveSchedule.value = it
                            getOnAirItem()
                        }

                    } else {

                    }
                }

                override fun onFailure(call: Call<RadioSchedule>, t: Throwable) {
//                    _artworkUrl.value = null
                }
            })
        }
    }

    override fun onCleared() {
        super.onCleared()
        player?.release() // Release the player when ViewModel is cleared
        player = null
    }
}


class SharedViewModelFactory(private val repository: Repository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
