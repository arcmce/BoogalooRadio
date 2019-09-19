package com.example.boogaloo.fragments


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
import kotlinx.android.synthetic.main.catchup_layout.*
import kotlinx.android.synthetic.main.livelayout.*
import org.json.JSONObject
import com.example.boogaloo.models.PlaylistResponse
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder



class CatchUpFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val rootMixCloudUrl = "https://api.mixcloud.com/" // BoogalooRadio/playlists/" // ['data'][x]['name']

    private lateinit var dataset: PlaylistResponse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.catchup_layout, container, false)

        sendRequests()

//        Log.d("CUF", mDataset.toString())

        // main catch up url: https://api.mixcloud.com/BoogalooRadio/playlists/


//        viewManager = GridLayoutManager(activity, 2)
////        viewAdapter = CatchUpAdapter(myDataset)
//
//        recyclerView = catch_up_recycler_view.apply {
//            setHasFixedSize(true)
//
//            layoutManager = viewManager
//
//            adapter = viewAdapter
//        }

        return view
    }

    fun sendRequests() {

        val playlistUrl = rootMixCloudUrl + "BoogalooRadio/playlists/"

        val playlistJSONRequest = JsonObjectRequest(Request.Method.GET, playlistUrl, null,
                Response.Listener<JSONObject> { response ->
                    val gson = GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()

//                    Log.d("CUF", response.toString())

                    dataset = gson.fromJson(response.toString(), PlaylistResponse::class.java)

                    Log.d("CUF", dataset.data.size.toString())
//                    Log.d("CUF", mDataset.toString())
                },
                Response.ErrorListener {}
        )

        VolleySingleton.getInstance(context!!.applicationContext).addToRequestQueue(playlistJSONRequest)
    }





}
