package com.arcmce.boogaloo.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.VolleySingleton
import com.arcmce.boogaloo.adapters.CatchUpAdapter
import com.arcmce.boogaloo.models.*
import org.json.JSONObject
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.catchup_layout.view.*


class CatchUpFragment : androidx.fragment.app.Fragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: CatchUpAdapter

    private val rootMixCloudUrl = "https://api.mixcloud.com/" // BoogalooRadio/playlists/" // ['data'][x]['name']

    private lateinit var playlistDataset: PlaylistResponse
    private val cloudcastMap = mutableMapOf<String, CloudcastResponse>()


    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.catchup_layout, container, false)

        sendPlaylistRequest()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.catch_up_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager =
                androidx.recyclerview.widget.GridLayoutManager(activity, 2)
        }
    }

    fun sendPlaylistRequest() {
        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/?limit=1000"
        sendMixcloudRequest(playlistUrl, null, ::playlistRequestCallback)
    }

    fun playlistRequestCallback(response: Map<String, Any?>) {
        Log.d("CUF", "playlistRequestCallback worked")

        playlistDataset = gson.fromJson(response["response"].toString(), PlaylistResponse::class.java)

        val initialDataset = ArrayList<RecyclerItem>()

        for (playlist in playlistDataset.data) {
            initialDataset.add(RecyclerItem(
                name = playlist.name,
                thumbnail = playlistDataset.data[0].owner.pictures.large,
                slug = playlist.slug
            ))
        }

        Log.d("CUF", "playlist dataset size" + playlistDataset.data.size.toString())


        viewAdapter = CatchUpAdapter(initialDataset) { slug ->
            sendCloudcastRequest(slug)
        }
        recyclerView.adapter = viewAdapter
    }

    fun sendCloudcastRequest(key: String) {
        Log.d("CUF", "callback " + key)

        //https://api.mixcloud.com/BoogalooRadio/playlists/eggy-elbows/cloudcasts/

        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/" + key + "/cloudcasts/"

        sendMixcloudRequest(playlistUrl, key, ::cloudcastRequestCallback)
    }

    fun cloudcastRequestCallback(response: Map<String, Any?>) {
        Log.d("CUF", response["slug"] as String + " cloudcastRequestCallback worked")

        val cloudcastData: CloudcastResponse = gson.fromJson(response["response"].toString(), CloudcastResponse::class.java)
        cloudcastMap[response["slug"] as String] = cloudcastData

        val updateDataset = ArrayList<RecyclerItem>()

        for (playlist in playlistDataset.data) {
            updateDataset.add(RecyclerItem(
                name = playlist.name,
                thumbnail = cloudcastMap.get(playlist.slug)?.data?.getOrNull(0)?.pictures?.large
                    ?: playlistDataset.data[0].owner.pictures.large,
                slug = playlist.slug
            ))
        }
        viewAdapter.updateList(updateDataset)
    }

    fun sendMixcloudRequest(mixcloudEndpoint: String, slug: String?, callback: (response: Map<String, Any?>) -> Unit) {
        val playlistJSONRequest = JsonObjectRequest(Request.Method.GET, mixcloudEndpoint, null,
                Response.Listener<JSONObject> { response ->
                    val responseObject = mapOf("response" to response, "slug" to slug)
                    callback(responseObject)
                },
                Response.ErrorListener {}
        )
        VolleySingleton.getInstance(context!!.applicationContext).addToRequestQueue(playlistJSONRequest)
    }
}
