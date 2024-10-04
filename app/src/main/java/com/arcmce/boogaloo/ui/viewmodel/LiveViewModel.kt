package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.arcmce.boogaloo.network.model.RadioInfo
import com.arcmce.boogaloo.network.repository.Repository
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.util.AppConstants
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LiveViewModel(private val repository: Repository, application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private var player: Player? = null
    val url = AppConstants.RADIO_STREAM_URL

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?> get() = _title

    private val _artworkUrl = MutableLiveData<String?>()
    val artworkUrl: LiveData<String?> get() = _artworkUrl

    fun setupPlayer() {
        // Check if the player has already been initialized
        if (player == null) {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

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
                        _artworkUrl.value = response.body()?.currentTrack?.artworkUrlLarge

                        val metadataArtist = title.value ?: AppConstants.DEFAULT_ARTIST
                        val artworkUri = artworkUrl.value.let { Uri.parse(it) }

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
