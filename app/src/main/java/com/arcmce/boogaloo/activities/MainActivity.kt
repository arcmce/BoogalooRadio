package com.arcmce.boogaloo.activities

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.adapters.TabAdapter
import com.arcmce.boogaloo.interfaces.ControlListener
import com.arcmce.boogaloo.services.MediaPlayerService
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.livelayout.*
import java.net.URL


class MainActivity : AppCompatActivity(), ControlListener {

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
            super.onMetadataChanged(metadata)
            updateUI(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            super.onPlaybackStateChanged(state)
        }
    }

    private fun updateUI(metadata: MediaMetadataCompat?) {
        textViewTrack.text = metadata?.description?.title

        Glide.with(applicationContext)
            .load(metadata?.description?.iconUri)
            .into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAI", "onCreate")

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            connectionCallbacks,
            null
        )

        setContentView(R.layout.activity_main)
        initializeUI()
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


    fun initializeUI() {
        Log.d("MAI", "initializeUI")

        setSupportActionBar(tool_bar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        tool_bar_logo.layoutParams.width = 350

//        tool_bar_logo.layoutParams.height = (tool_bar.layoutParams.height * 0.3).roundToInt()
        //        tool_bar_logo.
        tool_bar_logo.requestLayout()

//        setDisplayShowTitleEnabled(false)

        tab_layout.addTab(tab_layout.newTab().setText("Live"))
        tab_layout.addTab(tab_layout.newTab().setText("Catch Up"))
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL

        val tabsAdapter = TabAdapter(supportFragmentManager, tab_layout.tabCount)
        view_pager.adapter = tabsAdapter

        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager.currentItem = tab.position
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })
    }

    override fun playButtonClick() {
        Log.d("MAI", "playButtonClick")
        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)

        val pbState = mediaController?.playbackState?.state
        Log.d("MAI", "State: " + pbState.toString())
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }
    }
}
