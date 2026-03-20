package com.arcmce.boogaloo.playback


import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.arcmce.boogaloo.BuildConfig
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.arcmce.boogaloo.ui.view.MainActivity

class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d("PlaybackService", "PlaybackService created")

        // Initialize the ExoPlayer
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10_000, 30_000, 1_500, 3_000)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .setLoadControl(loadControl)
            .build()
        player.addAnalyticsListener(EventLogger())

        // Create a PendingIntent for launching the app when the notification is tapped
        val sessionActivityPendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create the MediaSession and link it to the player
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        // Create a MediaItem with metadata
        val mediaItem = MediaItem.Builder()
            .setUri("https://streams.radio.co/sb88c742f0/listen")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Boogaloo Radio")
                    .setArtist("Boogaloo Radio")
                    .build()
            )
            .build()

        // Set the media item to the player
        player.setMediaItem(mediaItem)

        // Prepare the player to play the media
        player.prepare()

        if (BuildConfig.DEBUG) Log.d("PlaybackService", "MediaSession created")

    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.let { player ->
            if (player.playWhenReady) {
                // Pause the player if it's playing
                player.pause()
            }
        }
        stopSelf()
    }

    override fun onDestroy() {
        if (BuildConfig.DEBUG) Log.d("PlaybackService", "onDestroy called")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
//        audioFocusManager.abandonAudioFocus()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        if (BuildConfig.DEBUG) Log.d("PlaybackService", "onGetSession called")
        return mediaSession
    }
}
