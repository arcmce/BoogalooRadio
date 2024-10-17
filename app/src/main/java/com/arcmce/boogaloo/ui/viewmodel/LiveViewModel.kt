package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.arcmce.boogaloo.network.model.RadioInfo
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


class LiveViewModel(private val repository: Repository, private val application: Application) : AndroidViewModel(application) {

    private var player: Player? = null

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?> get() = _title

    private val _artworkUrl = MutableStateFlow<String?>(null)
    val artworkUrl: StateFlow<String?> = _artworkUrl
    private var previousArtworkUrl: String? = null

    private val _artworkColor = MutableStateFlow(Color.Green)
    val artworkColor: StateFlow<Color> = _artworkColor


    fun loadImageAndExtractColors(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Load the image using Coil.
            val loader = ImageLoader(application.applicationContext)
            val request = ImageRequest.Builder(application.applicationContext)
                .data(imageUrl)
                .allowHardware(false) // Prevent hardware Bitmaps to enable Palette processing
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            bitmap?.let {
                // Generate a Palette from the Bitmap.
                Palette.from(it).generate { palette ->
                    // Extract the artwork color or use a default color.
                    val color = palette?.lightVibrantSwatch?.rgb
                        ?: palette?.lightMutedSwatch?.rgb
                        ?: palette?.mutedSwatch?.rgb
                        ?: palette?.vibrantSwatch?.rgb
                        ?: palette?.darkVibrantSwatch?.rgb
                        ?: palette?.darkMutedSwatch?.rgb
                        ?: Color.Gray.toArgb()
//                    val color = palette?.getMutedColor(Color.Red.toArgb()) ?: Color.Red.toArgb()
                    _artworkColor.value = Color(color)
                }
            }
        }
    }

    fun setupPlayer() {
        // Check if the player has already been initialized
        if (player == null) {

            val sessionToken = SessionToken(
                application.applicationContext,
                ComponentName(application.applicationContext, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(application.applicationContext, sessionToken).buildAsync()

            controllerFuture.addListener(
                {
                    player = controllerFuture.get()
                    // Use the player to update media items or other operations
                },
                MoreExecutors.directExecutor()
            )
        }
    }

    fun updateMetadata(metadataArtist: String, artworkUri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setUri(AppConstants.RADIO_STREAM_URL)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(AppConstants.RADIO_TITLE)
                    .setArtist(metadataArtist)
                    .setArtworkUri(artworkUri)
                    .build()
            )
            .build()

        // Update the player with the new media item
        player?.replaceMediaItem(0, mediaItem)
    }

    fun fetchRadioInfo() {
        viewModelScope.launch {
            val call = repository.getRadioInfo()
            call.enqueue(object : Callback<RadioInfo> {
                override fun onResponse(call: Call<RadioInfo>, response: Response<RadioInfo>) {
                    if (response.isSuccessful) {
                        _title.value = response.body()?.currentTrack?.title

                        val newArtworkUrl = response.body()?.currentTrack?.artworkUrlLarge
                        // this is outside of the if, because otherwise _artworkUrl will never get set after a theme change. its hacky though
                        _artworkUrl.value = newArtworkUrl

                        if (newArtworkUrl != previousArtworkUrl) {
//                            _artworkUrl.value = newArtworkUrl
                            previousArtworkUrl = newArtworkUrl

                            newArtworkUrl?.let { loadImageAndExtractColors(it) }
                        }
                        val metadataArtist = title.value ?: AppConstants.DEFAULT_ARTIST
                        val artworkUri = artworkUrl.value?.let { Uri.parse(it) } ?: Uri.EMPTY

                        updateMetadata(metadataArtist, artworkUri)

                    } else {
                        _artworkUrl.value = null
                    }
                }

                override fun onFailure(call: Call<RadioInfo>, t: Throwable) {
                    _artworkUrl.value = null
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

class LiveViewModelFactory(private val repository: Repository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
