package com.arcmce.boogaloo.services

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.arcmce.boogaloo.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.request.transition.Transition
import java.lang.NullPointerException
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    private var mMediaPlayer: MediaPlayer? = null
    private var radioUrl: String? = null

    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var prepared = false
    private var playWhenReady = false

    val delayedStopHandler = Handler()
    private var delayedStopRunnable = Runnable {stopMedia()}

    val ACTION_PLAY = "com.arcmce.boogaloo.ACTION_PLAY"
    val ACTION_PAUSE = "com.arcmce.boogaloo.ACTION_PAUSE"

//    private lateinit var mediaSessionManager: MediaSessionManager
    private var mediaSessionCompat: MediaSessionCompat? = null
//    private lateinit var transportControls: MediaController.TransportControls

    private lateinit var notification: NotificationCompat.Builder
    private val NOTIFICATION_ID = 312
    private val NOTIFICATION_CHANNEL = "media_player_channel"

    private var currentTrack: String? = "Live"
    private var currentTrackThumbnail: String? = ""

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("MPS", "onStartCommand: " + intent.action )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundNotification()
        }

        when (intent.action) {
            "init" -> {
                if (mMediaPlayer == null) {
                     try {
                        radioUrl = intent.extras!!.getString("media")
                    } catch (e: NullPointerException) {
                        stopSelf()
                    }

                    if (radioUrl != null && radioUrl != "") {
                        initMediaPlayer()
                    }
                } else {
                    removeDelayedStop()
                }
            }
            "toggle_play_pause" -> togglePlayPause()
            "update_notification_data" -> update_notification_data(intent)
        }

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(this, "MediaService")
    }

    override fun onDestroy() {
        Log.d("MPS", "onDestroy")
        super.onDestroy()
        stopMedia()
        mediaSessionCompat?.isActive = false
        removeDelayedStop()
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

            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            Log.d("MPS", "API level: ${Build.VERSION.SDK_INT}. Running deprecated AudioFocusRequest")
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
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
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    private fun togglePlayPause() {
        Log.d("MPS", "togglePlayPause")
        if (prepared) {
            if (mMediaPlayer?.isPlaying == true) {
                pauseMedia()
            } else {
                playMedia()
            }
        } else {
            Log.d("MPS", "togglePlayPause playWhenReady before $playWhenReady")
            playWhenReady = !playWhenReady
            Log.d("MPS", "togglePlayPause playWhenReady after $playWhenReady")
        }
    }

    private fun playMedia() {
        Log.d("MPS", "playMedia")
        if (prepared) {

            if (!requestAudioFocus()) {
                stopSelf()
            }

            removeDelayedStop()

            mMediaPlayer!!.start()
            playWhenReady = false

            buildNotification()
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun update_notification_data(intent: Intent) {
        currentTrack = intent.extras!!.getString("currentTrack")
        if (currentTrack == " - ") {currentTrack == "Live"}
        currentTrackThumbnail = intent.extras!!.getString("currentTrackThumbnail")
        if (mMediaPlayer?.isPlaying == true)  {
            buildNotification()
        }
    }

    private fun buildNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                notificationManager.createNotificationChannel(this)
            }
        }

        notification = NotificationCompat.Builder(applicationContext, "media_player_channel")
            .setContentTitle("Boogaloo Radio")
            .setContentText(currentTrack)
            .setSmallIcon(R.drawable.ic_boogaloo_logo)
//            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat?.sessionToken))
            .setVibrate(longArrayOf(0))

        Glide.with(this)
            .asBitmap()
            .load(currentTrackThumbnail)
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

        publishNotification()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundNotification() {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val name = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
            notificationManager.createNotificationChannel(this)
        }

        notification = NotificationCompat.Builder(applicationContext, "media_player_channel")
            .setContentTitle("Boogaloo Radio")
            .setSmallIcon(R.drawable.ic_boogaloo_logo)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat?.sessionToken))
            .setVibrate(longArrayOf(0))

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun publishNotification() {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notification.build())
        }
    }


}

