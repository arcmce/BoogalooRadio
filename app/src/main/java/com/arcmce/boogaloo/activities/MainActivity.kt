package com.arcmce.boogaloo.activities

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.fragments.*
import com.arcmce.boogaloo.models.CatchupRecyclerItem
import com.arcmce.boogaloo.models.CloudcastRecyclerItem
import com.arcmce.boogaloo.services.MediaPlayerService
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.live_layout.*


class MainActivity : AppCompatActivity(R.layout.activity_main),
    CatchUpFragment.CatchupListener,
    CloudcastFragment.CloudcastListener {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val connectionCallbacks = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d("MAI", "onConnected")
            mediaBrowser.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MainActivity,
                    token
                )

                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }

            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d("MAI", "onConnectionSuspended the service has crashed")
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.d("MAI", "onConnectionFailed: the service hasn't been able to connect")
        }
    }

    fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)

        val metadata = mediaController.metadata
        updateUI(metadata)
//        val pbState = mediaController.playbackState

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

            Log.d("MAI", "controller callback onmetadatachanged")
            super.onMetadataChanged(metadata)
            updateUI(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        }
    }

    private fun updateUI(metadata: MediaMetadataCompat?) {
        text_view_track.text = metadata?.description?.title

        Glide.with(applicationContext)
            .load(metadata?.description?.iconUri)
            .into(image_view)

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setSupportActionBar(tool_bar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        tool_bar_logo.layoutParams.width = 350

        tool_bar_logo.requestLayout()

        Log.d("MAI", "onCreate")

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            connectionCallbacks,
            null
        )

        if (savedInstanceState == null) {

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TabFragment>(R.id.fcw_main)
            }
        }

//        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()
        Log.d("MAI", mediaBrowser.isConnected().toString())

        Log.d("MAI", "onStart")
    }

    override fun onPause() {
        super.onPause()

        Log.d("MAI", "onPause")
    }

    override fun onStop() {
        super.onStop()

        mediaBrowser.disconnect()

        Log.d("MAI", "onStop")

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("MAI", "onDestroy")

    }

    override fun onResume() {
        super.onResume()
        Log.d("MAI", "onResume")
    }

    override fun onItemClicked(item: CatchupRecyclerItem) {
        Log.d("MAI", "onItemClicked " + item.slug)

        val fragment = CloudcastFragment.newInstance(item.slug)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
//            replace<CloudcastFragment>(R.id.fragment_container_view, item.slug)
            replace(R.id.fcw_main, fragment)
            addToBackStack(null)
        }

    }

    override fun onCloudcastItemClicked(item: CloudcastRecyclerItem) {
        Log.d("MAI", "onItemClicked " + item)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(item.url)
        startActivity(intent)

    }
}


