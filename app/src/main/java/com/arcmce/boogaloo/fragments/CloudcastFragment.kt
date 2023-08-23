package com.arcmce.boogaloo.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.adapters.CloudcastAdapter
import com.arcmce.boogaloo.models.CloudcastRecyclerItem
import com.arcmce.boogaloo.models.CloudcastResponse
import com.arcmce.boogaloo.network.MixCloudRequest
import com.arcmce.boogaloo.viewmodels.CloudcastViewModel
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.catchup_layout.view.*

class CloudcastFragment : androidx.fragment.app.Fragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: CloudcastAdapter
    private lateinit var mixCloudRequest: MixCloudRequest

    private lateinit var viewModel: CloudcastViewModel

    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    var activityCallback: CloudcastListener? = null

    interface CloudcastListener {
        fun onCloudcastItemClicked(item: CloudcastRecyclerItem)
    }

    companion object {
        const val ARG_SLUG = "slug"

        fun newInstance(slug: String): CloudcastFragment {
            val fragment = CloudcastFragment()

            val bundle = Bundle().apply {
                putString(ARG_SLUG, slug)
            }

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activityCallback = context as CloudcastListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement CatchupListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(CloudcastViewModel::class.java)

        val view = inflater.inflate(R.layout.cloudcast_layout, container, false)

        arguments?.getString(ARG_SLUG)?.let {
            viewModel.slug = it
        }

        Log.d("CCF", viewModel.slug + " cloudcast fragment onCreateView")

        mixCloudRequest = MixCloudRequest(requireContext().applicationContext)
        mixCloudRequest.getCloudcastData(viewModel.slug, ::cloudcastRequestCallback)

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

    fun cloudcastRequestCallback(response: Map<String, Any?>) {
        Log.d("CCF", response["slug"] as String + " cloudcastRequestCallback")


        val cloudcastData: CloudcastResponse = gson.fromJson(response["response"].toString(), CloudcastResponse::class.java)

        val dataset = ArrayList<CloudcastRecyclerItem>()
//        val sortedList = dataList.sortedBy { it.dateField }

        for (playlist in cloudcastData.data.sortedByDescending { it.createdTime }) {
            dataset.add(
                CloudcastRecyclerItem(
                    name = playlist.name,
                    thumbnail = playlist.pictures.large,
                    url = playlist.url,
                ))
        }

        viewAdapter = CloudcastAdapter(dataset) { item ->
            Log.d("CUF", "on click callback " + item.url)
            activityCallback?.onCloudcastItemClicked(item)
        }

        recyclerView.adapter = viewAdapter

    }

}