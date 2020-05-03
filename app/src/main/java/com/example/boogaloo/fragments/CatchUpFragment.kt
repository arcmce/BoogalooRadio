package com.example.boogaloo.fragments


import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.example.boogaloo.R
import com.example.boogaloo.VolleySingleton
import com.example.boogaloo.adapters.CatchUpAdapter
import com.example.boogaloo.models.*
import kotlinx.android.synthetic.main.catchup_layout.*
import kotlinx.android.synthetic.main.livelayout.*
import org.json.JSONObject
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.catchup_layout.view.*
import java.net.URL
import java.util.function.LongFunction


class CatchUpFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CatchUpAdapter//RecyclerView.Adapter<*>

    private val rootMixCloudUrl = "https://api.mixcloud.com/" // BoogalooRadio/playlists/" // ['data'][x]['name']

    private lateinit var playlistDataset: PlaylistResponse
    private lateinit var recyclerDataset: RecyclerviewModel
    private val cloudcastMap = mutableMapOf<String, CloudcastResponse>()


    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.catchup_layout, container, false)

//        viewAdapter = CatchUpAdapter(ArrayList()) { slug ->
//            sendCloudcastRequest(slug)
//        }
//        recyclerView.adapter = viewAdapter

        sendPlaylistRequest()

//        viewAdapter.

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.catch_up_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 2)
        }
    }

    fun sendPlaylistRequest() {
        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/?limit=1000"
        sendMixcloudRequest(playlistUrl, null, ::playlistRequestCallback)
    }

    fun playlistRequestCallback(response: Map<String, Any?>) {
        Log.d("CUF", "playlistRequestCallback worked")

        playlistDataset = gson.fromJson(response["response"].toString(), PlaylistResponse::class.java)

        recyclerDataset = RecyclerviewModel(data = ArrayList())

        for (playlist in playlistDataset.data) {
            recyclerDataset.data.add(RecyclerItem(
                name = playlist.name,
                thumbnail = playlist.owner.pictures.large,
                slug = playlist.slug
            ))
        }

        Log.d("CUF", "playlist dataset size" + playlistDataset.data.size.toString())
        Log.d("CUF", "recycler dataset size" + recyclerDataset.data.size.toString())


        viewAdapter = CatchUpAdapter(recyclerDataset.data) { slug ->
            sendCloudcastRequest(slug)
        }
        recyclerView.adapter = viewAdapter

        viewAdapter.updateList(recyclerDataset.data)
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

        for (playlist in recyclerDataset.data) {
            if (!cloudcastData.data.isEmpty() && playlist.slug == response["slug"]) {
                playlist.thumbnail = cloudcastData.data[0].pictures.large
            }
        }

        viewAdapter.updateList(recyclerDataset.data)
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
