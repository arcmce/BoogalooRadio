package com.arcmce.boogaloo.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.adapters.CatchUpAdapter
import com.arcmce.boogaloo.models.CloudcastResponse
import com.arcmce.boogaloo.models.PlaylistResponse
import com.arcmce.boogaloo.models.CatchupRecyclerItem
import com.arcmce.boogaloo.network.MixCloudRequest
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.catchup_layout.view.*


class CatchUpFragment : androidx.fragment.app.Fragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: CatchUpAdapter
    private lateinit var mixCloudRequest: MixCloudRequest

//    private val rootMixCloudUrl = "https://api.mixcloud.com/" // BoogalooRadio/playlists/" // ['data'][x]['name']

    private lateinit var playlistDataset: PlaylistResponse
    private val cloudcastMap = mutableMapOf<String, CloudcastResponse>()
    private lateinit var cloudcastUrlList: ArrayList<String>

    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    var activityCallback: CatchupListener? = null

    interface CatchupListener {
        fun onItemClicked(item: CatchupRecyclerItem)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activityCallback = context as CatchupListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement CatchupListener")
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is CatchupListener) {
//            activityCallback = context
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.catchup_layout, container, false)

        mixCloudRequest = MixCloudRequest(requireContext().applicationContext)

        mixCloudRequest.getPlaylistData(::playlistRequestCallback)

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

    fun playlistRequestCallback(response: Map<String, Any?>) {
        Log.d("CUF", "playlistRequestCallback")

        playlistDataset = gson.fromJson(response["response"].toString(), PlaylistResponse::class.java)

        val initialDataset = ArrayList<CatchupRecyclerItem>()
        cloudcastUrlList = ArrayList()

        for (playlist in playlistDataset.data) {
            initialDataset.add(CatchupRecyclerItem(
                name = playlist.name,
                thumbnail = playlistDataset.data[0].owner.pictures.large,
                slug = playlist.slug,
                url = cloudcastUrlList
            ))
        }

        Log.d("CUF", "playlist dataset size" + playlistDataset.data.size.toString())


        viewAdapter = CatchUpAdapter(initialDataset, { slug ->
            mixCloudRequest.getCloudcastData(slug, ::cloudcastRequestCallback)
        }, { item ->
            Log.d("CUF", "on click callback " + item.slug)
            activityCallback?.onItemClicked(item)
        })
        recyclerView.adapter = viewAdapter
    }

    fun cloudcastRequestCallback(response: Map<String, Any?>) {
        Log.d("CUF", response["slug"] as String + " cloudcastRequestCallback")

        val cloudcastData: CloudcastResponse = gson.fromJson(response["response"].toString(), CloudcastResponse::class.java)
        cloudcastMap[response["slug"] as String] = cloudcastData

        val updateDataset = ArrayList<CatchupRecyclerItem>()
        cloudcastUrlList = ArrayList()

        for (playlist in playlistDataset.data) {
            updateDataset.add(CatchupRecyclerItem(
                name = playlist.name,
                thumbnail = cloudcastMap[playlist.slug]?.data?.getOrNull(0)?.pictures?.large
                    ?: playlistDataset.data[0].owner.pictures.large,
                slug = playlist.slug,
                url = if (cloudcastMap[playlist.slug]?.data.isNullOrEmpty()) cloudcastUrlList else ArrayList(cloudcastMap[playlist.slug]?.data?.map { it.url }!!)
            ))
        }
        viewAdapter.updateList(updateDataset)
    }
}
