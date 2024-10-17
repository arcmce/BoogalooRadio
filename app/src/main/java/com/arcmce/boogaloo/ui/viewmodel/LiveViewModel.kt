package com.arcmce.boogaloo.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
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
import com.arcmce.boogaloo.network.model.RadioInfo
import com.arcmce.boogaloo.network.repository.Repository
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.util.AppConstants
import com.google.common.util.concurrent.MoreExecutors
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

    init {
        setupPlayer()
    }

    fun setupPlayer() {
        if (player == null) {

            val sessionToken = SessionToken(
                application.applicationContext,
                ComponentName(application.applicationContext, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(application.applicationContext, sessionToken).buildAsync()

            controllerFuture.addListener(
                {
                    player = controllerFuture.get()
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
                        _artworkUrl.value = newArtworkUrl

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
