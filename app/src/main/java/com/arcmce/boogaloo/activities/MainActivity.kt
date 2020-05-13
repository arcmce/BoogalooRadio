package com.arcmce.boogaloo.activities

import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.*
import com.google.android.material.tabs.TabLayout
import com.arcmce.boogaloo.interfaces.ControlListener
import com.arcmce.boogaloo.services.MediaPlayerService
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.VolleySingleton
import com.arcmce.boogaloo.adapters.TabAdapter
import kotlinx.android.synthetic.main.livelayout.*


class MainActivity : AppCompatActivity(), ControlListener {

    lateinit var handler: Handler
    lateinit var networkRunnable: Runnable

    val radioUrl: String = "https://streams.radio.co/sb88c742f0/listen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAI", "onCreate")

        setContentView(R.layout.activity_main)
        initializeUI()

        startUpdatingUI()

        initializeMediaPlayer(radioUrl)
    }

    override fun onStart() {
        super.onStart()

        Log.d("MAI", "onStart")
    }

    override fun onPause() {
        super.onPause()

        Log.d("MAI", "onPause")
    }

    override fun onStop() {
        super.onStop()

        Log.d("MAI", "onStop")

        handler.removeCallbacks(networkRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("MAI", "onDestroy")

    }

    override fun onResume() {
        super.onResume()

        Log.d("MAI", "onResume")

        startUpdatingUI()

        initializeMediaPlayer(radioUrl)
    }

    fun initializeMediaPlayer(radioUrl: String) {
        Log.d("MAI", "initializeMediaPlayer")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(this, MediaPlayerService::class.java).apply {
                this.action = "init"
                this.putExtra("media", radioUrl)
                startForegroundService(this)
            }
        } else {
            Intent(this, MediaPlayerService::class.java).apply {
                this.action = "init"
                this.putExtra("media", radioUrl)
                startService(this)
            }
        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(this, MediaPlayerService::class.java).apply {
                this.action = "toggle_play_pause"
                startForegroundService(this)
            }
        } else {
            Intent(this, MediaPlayerService::class.java).apply {
                this.action = "toggle_play_pause"
                startService(this)
            }
        }
    }

    fun startUpdatingUI() {
        Log.d("MAI", "start_updating_ui")

        handler = Handler()
        networkRunnable = Runnable {
            updateRadioInfo()

            handler.postDelayed(
                networkRunnable,
                60000
            )
        }
        handler.post(networkRunnable)
    }

    fun updateRadioInfo() {
        Log.d("MAI", "updateRadioInfo")

        val url = "https://public.radio.co/stations/sb88c742f0/status"

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->

                val jsonResponse = JSONObject(response)
                val strCurrentTrack: String = jsonResponse.getJSONObject(
                    "current_track")
                    .get("title").toString()
                val strArtworkUrl: String = jsonResponse.getJSONObject(
                    "current_track").
                    get("artwork_url_large").toString()

                if (strCurrentTrack == " - ") {strCurrentTrack == "Boogaloo Radio - Live"}

                textViewTrack.text = strCurrentTrack

                DownloadImageTask(imageView).execute(
                    strArtworkUrl
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(this, MediaPlayerService::class.java).apply {
                        this.action = "update_notification_data"
                        this.putExtra("currentTrack", strCurrentTrack)
                        this.putExtra("currentTrackThumbnail", strArtworkUrl)
                        startForegroundService(this)
                    }
                } else {
                    Intent(this, MediaPlayerService::class.java).apply {
                        this.action = "update_notification_data"
                        this.putExtra("currentTrack", strCurrentTrack)
                        this.putExtra("currentTrackThumbnail", strArtworkUrl)
                        startService(this)
                    }
                }




            },
            Response.ErrorListener {}
        )

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }
}


private class DownloadImageTask(internal val imageView : ImageView) : AsyncTask<String, Void, Bitmap?>() {
    override fun doInBackground(vararg params: String?): Bitmap? {
        val imageUrl = params[0]
        return try {
            val inputStream = URL(imageUrl).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result!=null) {
            imageView.setImageBitmap(result)
        } else {
            Toast.makeText(imageView.context,"Error downloading",Toast.LENGTH_SHORT).show()
        }
    }
}
