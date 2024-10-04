package com.arcmce.boogaloo.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.activities.MainActivity
import com.arcmce.boogaloo.network.RadioInfoRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaPlayerService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    private var isServiceStarted: Boolean = false

    private var mMediaPlayer: MediaPlayer? = null
    private val radioUrl: String = "https://streams.radio.co/sb88c742f0/listen"

    private var audioManager: AudioManager? = null
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var prepared = false
    private var playWhenReady = false

    private val delayedStopHandler = Handler()
    private var delayedStopRunnable = Runnable {stopMedia()}

    private val nowPlayingHandler = Handler()
    lateinit var nowPlayingRunnable: Runnable

    val ACTION_PLAY = "com.arcmce.boogaloo.ACTION_PLAY"
    val ACTION_PAUSE = "com.arcmce.boogaloo.ACTION_PAUSE"

    private var mediaSessionCompat: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var transportControls: MediaControllerCompat.TransportControls? = null

    private lateinit var radioInfoRequest: RadioInfoRequest

    private lateinit var notification: NotificationCompat.Builder
    private val NOTIFICATION_ID = 312 //random number
    private val NOTIFICATION_CHANNEL = "media_player_channel"

    private val EMPTY_MEDIA_ROOT_ID = "empty_root_id"

    private fun setPlaybackState(state: Int) {
        stateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            stateBuilder.setActions(
                PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        } else {
            stateBuilder.setActions(
                PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        }
        stateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F)
        mediaSessionCompat?.setPlaybackState(stateBuilder.build())
    }

    private val mediaSessionCallbacks = object: MediaSessionCompat.Callback() {
        override fun onPlay() {
            if (requestAudioFocus()) {

                startService(Intent(applicationContext, MediaPlayerService::class.java))
                mediaSessionCompat!!.isActive = true
                playMedia()

                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)

                buildNotification()
                startForeground(NOTIFICATION_ID, notification.build())
            }
        }

        override fun onStop() {
            stopSelf()
            mediaSessionCompat!!.isActive = false
            stopMedia()
            stopForeground(true)
        }

        override fun onPause() {
            pauseMedia()

            setPlaybackState(PlaybackStateCompat.STATE_PAUSED)

            buildNotification()
            publishNotification()

            stopForeground(false)
        }
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("MPS", "onStartCommand: " + intent.action )

        isServiceStarted = true

        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MPS", "onCreate")

        radioInfoRequest = RadioInfoRequest(this)

        startUpdatingUI()

        initMediaPlayer()

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val pendingItent = PendingIntent.getBroadcast(
            baseContext,
            0, mediaButtonIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
//
//        mediaSession = MediaSessionCompat(baseContext, TAG, null, pendingItent).also {
//            it.isActive = true
//        }
//
//        sessionToken = mediaSession.sessionToken
//        packageValidator = PackageValidator(this@MediaService, R.xml.allowed_media_browser_callers)


        // media session
        mediaSessionCompat = MediaSessionCompat(this, "MediaService", null, pendingItent).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())

            setCallback(mediaSessionCallbacks)

            setSessionToken(sessionToken)
        }

        transportControls = mediaSessionCompat?.controller?.transportControls

        // metadata
        val metadataBuilder = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Boogaloo")
        }
        mediaSessionCompat?.setMetadata(metadataBuilder.build())

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == EMPTY_MEDIA_ROOT_ID) {
            result.sendResult(null)
            return
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onDestroy() {
        Log.d("MPS", "onDestroy")
        super.onDestroy()
        stopMedia()
        mediaSessionCompat?.isActive = false
        removeDelayedStop()
        removeNowPlayingHandler()
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        Log.d("MPS", "buffering: $percent")
    }

    override fun onCompletion(mp: MediaPlayer) {
        Log.d("MPS", "onCompletion")
        //Invoked when playback of a media source has completed.
        stopMedia()
        //stop the service
        stopSelf()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.d("MPS", "onError")
        //Invoked when there has been an error during an asynchronous operation.
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MPSE",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MPSE",
                "MEDIA ERROR SERVER DIED $extra"
            )
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MPSE",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        Log.d("MPS", "onPrepared")
        prepared = true
        if (playWhenReady) {
            Log.d("MPS", "onPrepared, play when ready true")
            playMedia()
        }
    }

    override fun onAudioFocusChange(focusStatus: Int) {
        Log.d("MPS", "onAudioFocusChange")
        //Invoked when the audio focus of the system is updated.

        when (focusStatus) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d("MPS", "AUDIOFOCUS_GAIN")
                mMediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d("MPS", "AUDIOFOCUS_LOSS")
                pauseMedia()
                delayedStopHandler.postDelayed(
                    delayedStopRunnable,
                    TimeUnit.SECONDS.toMillis(30)
                )
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d("MPS", "AUDIOFOCUS_LOSS_TRANSIENT")
                pauseMedia()

            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d("MPS", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                if (mMediaPlayer?.isPlaying == true) {
                    mMediaPlayer!!.setVolume(0.1f, 0.1f)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus(): Boolean {
        Log.d("MPS", "requestAudioFocus")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val focusResult = if (Build.VERSION.SDK_INT >= 26) {
            Log.d("MPS", "API level: ${Build.VERSION.SDK_INT}. Running new AudioFocusRequest")
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@MediaPlayerService)
                build()
            }

            audioManager!!.requestAudioFocus(audioFocusRequest)
        } else {
            Log.d("MPS", "API level: ${Build.VERSION.SDK_INT}. Running deprecated AudioFocusRequest")
            audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true
        }
        //Could not gain focus
        return false
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun removeAudioFocus(): Boolean {
        Log.d("MPS", "removeAudioFocus")
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager?.abandonAudioFocusRequest(audioFocusRequest)
    }

    private fun playMedia() {
        Log.d("MPS", "playMedia")
        if (prepared) {
            removeDelayedStop()

            mMediaPlayer!!.start()
            playWhenReady = false

            buildNotification()
            publishNotification()
            Log.d("MPS", "playMedia starting")
        } else {
            playWhenReady = true
        }
    }

    private fun stopMedia() {
        Log.d("MPS", "stopMedia")
        mMediaPlayer?.let {
            it.stop()
            it.release()
        }
        mMediaPlayer = null
        prepared = false
        removeAudioFocus()
    }

    private fun pauseMedia() {
        Log.d("MPS", "pauseMedia")
        playWhenReady = false
        if (mMediaPlayer?.isPlaying == true)  {
            mMediaPlayer!!.pause()
        }
    }

    private fun initMediaPlayer() {
        Log.d("MPS", "initMediaPlayer")
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        }

        mMediaPlayer!!.setOnCompletionListener(this)
        mMediaPlayer!!.setOnErrorListener(this)
        mMediaPlayer!!.setOnPreparedListener(this)

        //Reset so that the MediaPlayer is not pointing to another data source
        mMediaPlayer!!.reset()

        try {
            Log.d("MPS", "try set data source: $radioUrl")
            mMediaPlayer!!.setDataSource(radioUrl)
        }  catch (e: IOException) {
            Log.d("MPSE", "set data source exception")
            e.printStackTrace()
            stopSelf()
        }
        prepared = false
        mMediaPlayer!!.prepareAsync()

    }

    private fun removeDelayedStop() {delayedStopHandler.removeCallbacks(delayedStopRunnable)}

    private fun startUpdatingUI() {
        Log.d("MPS", "start_updating_ui")

        nowPlayingRunnable = Runnable {
            radioInfoRequest.getRadioInfo(::updateMetadata)

            nowPlayingHandler.postDelayed(
                nowPlayingRunnable,
                10000
            )
        }
        nowPlayingHandler.post(nowPlayingRunnable)
    }

    private fun removeNowPlayingHandler() {nowPlayingHandler.removeCallbacks(nowPlayingRunnable)}

    private fun buildNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                notificationManager.createNotificationChannel(this)
            }
        }

        val metadata = mediaSessionCompat?.controller?.metadata
//        metadata?.description?.title


        val mainActivityIntent = Intent(this, MainActivity::class.java)
//        val contentInent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)
        val contentIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val playbackState = mediaSessionCompat?.controller?.playbackState
        val playPauseIcon = if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING)
            R.drawable.ic_media_pause else R.drawable.ic_media_play

        notification = NotificationCompat.Builder(applicationContext, "media_player_channel")
            .setContentTitle(metadata?.description?.title)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat?.sessionToken)
                .setShowActionsInCompactView(0))
            .setVibrate(longArrayOf(0))
            .setShowWhen(false)
//            .setContentIntent(contentIntent)
            .setContentIntent(contentIntent)
//            .addAction(
//                NotificationCompat.Action(
//                    playPauseIcon,
//                    "pause",
//                    MediaButtonReceiver.buildMediaButtonPendingIntent(
//                        this,
//                        PlaybackStateCompat.ACTION_PLAY_PAUSE,
//                    )
//                )
//            )

        Glide.with(this)
            .asBitmap()
            .load(metadata?.description?.iconUri)
            .into(object: CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("MPS", "glide callback")
                    notification.setLargeIcon(resource)
                    publishNotification()
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("MPS", "onLoadCleared has been called!")
                }
            })

//        return notification
    }

    private fun publishNotification() {
//        with(NotificationManagerCompat.from(this)) {
//            notify(NOTIFICATION_ID, notification.build())
//        }
    }

    private fun updateMetadata(response: String) {

        Log.d("MPS", "updateMetadata")

        val jsonResponse = JSONObject(response)
        var strCurrentTrack: String = jsonResponse.getJSONObject(
            "current_track")
            .get("title").toString()
        val strArtworkUrl: String = jsonResponse.getJSONObject(
            "current_track").
            get("artwork_url_large").toString()

        if (strCurrentTrack == " - ") {strCurrentTrack = "Boogaloo Radio - Live"}


        val metadataBuilder = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, strCurrentTrack)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, strArtworkUrl)
        }

        mediaSessionCompat?.setMetadata(metadataBuilder.build())

//        if

        val playbackState = mediaSessionCompat?.controller?.playbackState
        if (playbackState?.state != PlaybackStateCompat.STATE_NONE) {
            buildNotification()
            publishNotification()
        }
    }

}

