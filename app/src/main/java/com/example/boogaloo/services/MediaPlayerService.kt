package com.example.boogaloo.services

import android.annotation.TargetApi
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.os.Build
import java.lang.NullPointerException


class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    private val binder = LocalBinder()

//    private lateinit var mMediaPlayer: MediaPlayer
    private var mMediaPlayer: MediaPlayer? = null
    private var radioUrl: String? = null

    //Used to pause/resume MediaPlayer
//    private var resumePosition: Int = 0

    private lateinit var audioManager: AudioManager
//    private var audioFocusRequest: AudioFocusRequest? = null
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var prepared = false
    private var playWhenReady = false


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("MPS", "onStartCommand")

        when (intent.action) {
            "init" -> {
                try {
                    radioUrl = intent.extras!!.getString("media")
                } catch (e: NullPointerException) {
                    stopSelf()
                }

                if (!requestAudioFocus()) {
                    stopSelf()
                }

                if (radioUrl != null && radioUrl != "") {
                    initMediaPlayer()
                }
            }
            "toggle_play_pause" -> togglePlayPause()
        }


//        if (intent!!.action == "toggle_play_pause") {
//            togglePlayPause()
//        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d("MPS", "onDestroy")
        super.onDestroy()
        if (mMediaPlayer != null) {
            stopMedia()
            mMediaPlayer?.release()

        }
        removeAudioFocus()
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
        Log.d("MediaPlayerService", "onPrepared")
        prepared = true
        if (playWhenReady) {
            playMedia()
        }
    }

    override fun onAudioFocusChange(focusStatus: Int) {
        Log.d("MPS", "onAudioFocusChange")
        //Invoked when the audio focus of the system is updated.
        when (focusStatus) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer()
                } else if (mMediaPlayer?.isPlaying == false) {
                    mMediaPlayer!!.start()
                }
                mMediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mMediaPlayer?.isPlaying == true) {
                    mMediaPlayer!!.stop()
                }
                mMediaPlayer!!.release()
                mMediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mMediaPlayer?.isPlaying == true) {
                    mMediaPlayer!!.pause()
                }

            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
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
            mMediaPlayer!!.start()
            Log.d("MediaPlayerService", "playMedia starting")
        } else {
            playWhenReady = true
        }
//        if (mMediaPlayer?.isPlaying == false) {
//            mMediaPlayer!!.start()
//        }
    }

    private fun stopMedia() {
        Log.d("MPS", "stopMedia")
        if (mMediaPlayer?.isPlaying == false) {
            mMediaPlayer?.stop()
            prepared = false
        }
    }

    private fun pauseMedia() {
        Log.d("MPS", "pauseMedia")
        playWhenReady = false
        if (mMediaPlayer?.isPlaying == true)  {
            mMediaPlayer!!.pause()
//            resumePosition = mMediaPlayer.getCurrentPosition()
        }
    }

//    private fun resumeMedia() {
//        Log.d("MPS", "resumeMedia")
//        if (mMediaPlayer?.isPlaying == false) {
////            mMediaPlayer.seekTo(resumePosition)
//            mMediaPlayer!!.start()
//        }
//    }

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
//            mMediaPlayer.setDataSource("https://streams.radio.co/sb88c742f0/listen")
        }  catch (e: IOException) {
            Log.d("MPSE", "set data source exception")
            e.printStackTrace()
            stopSelf()
        }
        prepared = false
        mMediaPlayer!!.prepareAsync()

    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("MPS", "onBind")
        return binder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }
}
