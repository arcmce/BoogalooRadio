package com.example.boogaloo.activities

import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import android.os.IBinder
import android.content.Intent
import android.widget.*
import android.support.design.widget.TabLayout
import com.example.boogaloo.interfaces.ControlListener
import com.example.boogaloo.services.MediaPlayerService
import com.example.boogaloo.R
import com.example.boogaloo.VolleySingleton
import com.example.boogaloo.adapters.TabAdapter
import kotlinx.android.synthetic.main.livelayout.*


class MainActivity : AppCompatActivity(), ControlListener {

    lateinit var handler: Handler
    lateinit var networkRunnable: Runnable

    val radioUrl: String = "https://streams.radio.co/sb88c742f0/listen"

    lateinit var playerService: MediaPlayerService
    var serviceBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAI", "onCreate")

        setContentView(R.layout.activity_main)
        initializeUI()

        startUpdatingUI()

        playAudio(radioUrl)
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

        if (serviceBound) {
            unbindService(serviceConnection)
            playerService.stopSelf()
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("MAI", "onResume, $serviceBound")

        startUpdatingUI()
    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("MAI", "onServiceConnected called")

            val binder = service as MediaPlayerService.LocalBinder
            playerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MAI", "onServiceDisconnected")

            serviceBound = false
        }
    }

    fun playAudio(radioUrl: String) {
        Log.d("MAI", "playAudio")

        if (!serviceBound) {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            playerIntent.action = "init"
            playerIntent.putExtra("media", radioUrl)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            // active
        }
    }

    fun initializeUI() {
        Log.d("MAI", "initializeUI")

        setSupportActionBar(tool_bar)

        tab_layout.addTab(tab_layout.newTab().setText("Live"))
        tab_layout.addTab(tab_layout.newTab().setText("Catch Up"))
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL

        val tabsAdapter =
            TabAdapter(supportFragmentManager, tab_layout.tabCount)
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
        val playerIntent = Intent(this, MediaPlayerService::class.java)
        playerIntent.action = "toggle_play_pause"
        startService(playerIntent)
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

                textViewTrack.text = strCurrentTrack

                //TODO only do this if url changes
                DownloadImageTask(imageView).execute(
                    strArtworkUrl
                )

            },
            Response.ErrorListener {}
        )

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }
}


private class DownloadImageTask(internal val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
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
