package com.arcmce.boogaloo.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.arcmce.boogaloo.models.PlaylistResponse

class MixCloudRequest(val context: Context) {
    private val rootMixCloudUrl = "https://api.mixcloud.com/"
    private lateinit var playlistDataset: PlaylistResponse

    fun getPlaylistData(callback: (response: Map<String, Any?>) -> Unit) {
        Log.d("MCR", "request playlist")

        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/?limit=1000"

        sendMixcloudRequest(playlistUrl, null, callback)
    }

    fun getCloudcastData(key: String, callback: (response: Map<String, Any?>) -> Unit) {
        Log.d("MCR", "request cloudcast: " + key)

        //https://api.mixcloud.com/BoogalooRadio/playlists/eggy-elbows/cloudcasts/

        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/" + key + "/cloudcasts/"

        sendMixcloudRequest(playlistUrl, key, callback)
    }

    fun sendMixcloudRequest(mixcloudEndpoint: String, slug: String?, callback: (response: Map<String, Any?>) -> Unit) {
        val playlistJSONRequest = JsonObjectRequest(
            Request.Method.GET, mixcloudEndpoint, null,
            { response ->
                val responseObject = mapOf("response" to response, "slug" to slug)
                callback(responseObject)
            },
            {}
        )
        VolleySingleton.getInstance(context).addToRequestQueue(playlistJSONRequest)
    }
}