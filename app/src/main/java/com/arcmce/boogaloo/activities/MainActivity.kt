package com.arcmce.boogaloo.activities

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.databinding.ActivityMainBinding
import com.arcmce.boogaloo.databinding.LiveLayoutBinding
import com.arcmce.boogaloo.fragments.*
import com.arcmce.boogaloo.models.CatchupRecyclerItem
import com.arcmce.boogaloo.models.CloudcastRecyclerItem
import com.arcmce.boogaloo.services.MediaPlayerService


class MainActivity : AppCompatActivity(R.layout.activity_main),
    CatchUpFragment.CatchupListener,
    CloudcastFragment.CloudcastListener {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private lateinit var activityMainBinding: ActivityMainBinding
//    private lateinit var i

    private val connectionCallbacks = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d("MAI", "onConnected mediabrowser connected")
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
        Log.d("MAI", "buildTransportControls pre updateui")
        updateUI(metadata)
//        val pbState = mediaController.playbackState

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

            Log.d("MAI", "onmetadatachanged pre updateui")
            updateUI(metadata)
//            super.onMetadataChanged(metadata)
        }

//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//
//            Log.d("MAI", "onPlaybackStateChanged")
//        }
    }

    private fun updateUI(metadata: MediaMetadataCompat?) {

        Log.d("MAI", "updateUI")

        val tabFrag = supportFragmentManager.findFragmentByTag("tabfrag") as TabFragment
        metadata?.description?.iconUri?.let { it
            tabFrag.tabsAdapter?.liveFragment?.updateThumbnail(it.toString())
        }

        val contFrag = supportFragmentManager.findFragmentByTag("contfrag") as ControlsFragment
        contFrag.updateShowText(metadata?.description?.title as String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
//        val view = binding.root
//        setContentView(view)
//        liveLayoutBinding = LiveLayoutBinding.inflate(layoutInflater)

        setSupportActionBar(activityMainBinding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        activityMainBinding.toolBarLogo.layoutParams.width = 350

        activityMainBinding.toolBarLogo.requestLayout()

        Log.d("MAI", "onCreate")



        val navController = findNavController(activityMainBinding.navHostFragment)
        val bottomNavigationView = activityMainBinding.bottomNavigation

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    navController.navigate(liveFragment)
                    true
                }
                R.id.item_2 -> {
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            connectionCallbacks,
            null
        )

        if (savedInstanceState == null) {

            Log.d("MAI", "oncreate commit")
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TabFragment>(activityMainBinding.fcvMain.id, "tabfrag")
                add<ControlsFragment>(activityMainBinding.fcvCont.id, "contfrag")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()

        Log.d("MAI", "onStart")
    }

    override fun onPause() {
        super.onPause()

        Log.d("MAI", "onPause")
    }

    override fun onStop() {
        super.onStop()

        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)

        mediaController.unregisterCallback(controllerCallback)

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
            replace(R.id.fcv_main, fragment)
            addToBackStack(null)
        }

    }

    fun launchUriIntent(uri: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
//        startActivity(intent)

    }

    override fun onCloudcastItemClicked(item: CloudcastRecyclerItem) {
        Log.d("MAI", "onItemClicked " + item)

        launchUriIntent(item.url)
//
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.parse(item.url)
//        startActivity(intent)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.boog_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.website -> {

                val site = "https://boogalooradio.com"
//                val intent = Intent(Intent.ACTION_VIEW)
//                val uri = Uri.parse(site)
//                intent.data = uri
//
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                }

                launchUriIntent(site)

                true
            }
            R.id.instagram -> {

                val instagramUsername = "theboogaloopub"

//                val intent = Intent(Intent.ACTION_VIEW)
//                val uri = Uri.parse("https://www.instagram.com/$instagramUsername")
//                intent.data = uri
//
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                }

                launchUriIntent("https://www.instagram.com/$instagramUsername")

                true
            }
            R.id.facebook -> {

                val facebookUsername = "boogaloopub"

//                val intent = Intent(Intent.ACTION_VIEW)
//                val uri = Uri.parse("https://www.facebook.com/$facebookUsername")
//                intent.data = uri
//
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                }

                launchUriIntent("https://www.facebook.com/$facebookUsername")

                true
            }
            R.id.twitter -> {

                val twitterUsername = "TheBoogaloo"

//                val intent = Intent(Intent.ACTION_VIEW)
//                val uri = Uri.parse("https://twitter.com/$twitterUsername")
//                intent.data = uri
//
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                }

                launchUriIntent("https://twitter.com/$twitterUsername")

                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }


}


