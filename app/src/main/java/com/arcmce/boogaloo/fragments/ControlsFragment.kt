package com.arcmce.boogaloo.fragments

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
//import com.arcmce.boogaloo.interfaces.ControlListener
import com.arcmce.boogaloo.services.MediaPlayerService
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.controls_layout.view.*


class ControlsFragment : androidx.fragment.app.Fragment() {

    private lateinit var mediaBrowser: MediaBrowserCompat

//    lateinit var callback: ControlListener

    private val connectionCallbacks = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    activity,
                    token
                )

                MediaControllerCompat.setMediaController(activity!!, mediaController)
            }

            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(activity!!)

//        val metadata = mediaController.metadata
//        val pbState = mediaController.playbackState

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            updatePlayPauseButton(state)

        }
    }

    private fun updatePlayPauseButton(state: PlaybackStateCompat?) {
        val playPauseIcon = if (state?.state == PlaybackStateCompat.STATE_PLAYING)
            R.drawable.ic_media_pause else R.drawable.ic_media_play


        view?.playButton?.setImageResource(playPauseIcon)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(activity!!, MediaPlayerService::class.java),
            connectionCallbacks,
            null
        )
    }

    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()
        Log.d("CON", mediaBrowser.isConnected().toString())

        Log.d("CON", "onStart")
    }

    override fun onPause() {
        super.onPause()

        Log.d("CON", "onPause")
    }

    override fun onStop() {
        super.onStop()

        mediaBrowser.disconnect()

        Log.d("CON", "onStop")

    }

//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//        if (context is ControlListener) {
//            callback = context
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.controls_layout, container, false)

        view.playButton.setOnClickListener {
            playButtonClick()
        }
        return view

    }


    fun playButtonClick() {
        Log.d("CON", "playButtonClick")
        val mediaController = MediaControllerCompat.getMediaController(activity!!)

        val pbState = mediaController?.playbackState?.state
        Log.d("CON", "State: " + pbState.toString())
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {

            Log.d("CON", "State: " + pbState.toString())
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }
    }

}



