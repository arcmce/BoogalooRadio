package com.arcmce.boogaloo.fragments

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.network.RadioInfoRequest
import com.arcmce.boogaloo.services.MediaPlayerService
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.controls_layout.*
import kotlinx.android.synthetic.main.controls_layout.image_view
import kotlinx.android.synthetic.main.controls_layout.view.*
import kotlinx.android.synthetic.main.live_layout.*
import org.json.JSONObject

class ControlsFragment : androidx.fragment.app.Fragment() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var radioInfoRequest: RadioInfoRequest

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
            setInitialPlaybackState()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    fun buildTransportControls() {
        Log.d("CON", "buildTransportControls")
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
//        val mediaController = MediaControllerCompat.getMediaController(this@)

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d("CON", "onPlaybackStateChanged")
            updatePlayPauseButton(state)

        }
    }

    fun updateShowText(title: String) {
//        Log.d("CON", "updateShowText")

        text_view_track.let {
            it.text = title
        }
    }

    fun setInitialPlaybackState() {

        Log.d("CON", "setInitialPlaybackState")

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())

        updatePlayPauseButton(mediaController.playbackState)
    }

    private fun updatePlayPauseButton(state: PlaybackStateCompat?) {

        Log.d("CON", "updatePlayPauseButton " + state?.state.toString())

        val playPauseIcon = if (state?.state == PlaybackStateCompat.STATE_PLAYING)
            R.drawable.ic_media_pause else R.drawable.ic_media_play

        view?.playButton?.setImageResource(playPauseIcon)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(requireActivity(), MediaPlayerService::class.java),
            connectionCallbacks,
            null
        )
    }

    override fun onStart() {
        super.onStart()

        if (!mediaBrowser.isConnected) {
            mediaBrowser.connect()

        }
        Log.d("CON", mediaBrowser.isConnected().toString())

        Log.d("CON", "onStart")
    }

    override fun onPause() {
        super.onPause()

        Log.d("CON", "onPause")
    }

    override fun onStop() {
        super.onStop()

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())

        mediaController.unregisterCallback(controllerCallback)

        mediaBrowser.disconnect()

        Log.d("CON", "onStop")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.controls_layout, container, false)

        radioInfoRequest = RadioInfoRequest(requireContext().applicationContext)
        radioInfoRequest.getRadioInfo(::radioInfoRequestCallback)

        view.playButton.setOnClickListener {
            playButtonClick()
        }
        return view

    }

    fun playButtonClick() {
        Log.d("CON", "playButtonClick")
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())

        val pbState = mediaController?.playbackState?.state
        Log.d("CON", "playButtonClick - current state: " + pbState.toString())
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {

//            Log.d("CON", "State: " + pbState.toString())
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }
    }

    fun radioInfoRequestCallback(response: String) {

        val jsonResponse = JSONObject(response)
        val strCurrentTrack: String = jsonResponse.getJSONObject(
            "current_track")
            .get("title").toString()

        updateShowText(strCurrentTrack)

    }

}



