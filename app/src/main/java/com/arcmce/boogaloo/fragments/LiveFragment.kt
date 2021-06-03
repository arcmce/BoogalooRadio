package com.arcmce.boogaloo.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.network.RadioInfoRequest
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.live_layout.*
import org.json.JSONObject

class LiveFragment : androidx.fragment.app.Fragment() {

    private lateinit var radioInfoRequest: RadioInfoRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LVF", "onCreateView")

        radioInfoRequest = RadioInfoRequest(requireContext().applicationContext)

        radioInfoRequest.getRadioInfo(::radioInfoRequestCallback)

        return inflater.inflate(R.layout.live_layout, container, false)

    }

    fun radioInfoRequestCallback(response: String) {

        val jsonResponse = JSONObject(response)
        val strArtworkUrl: String = jsonResponse.getJSONObject(
            "current_track").
            get("artwork_url_large").toString()

        updateThumbnail(strArtworkUrl)
    }


    fun updateThumbnail(strArtworkUrl: String) {

        Log.d("LVF", "updateThumbnail")

        image_view?.let {
            Glide.with(requireContext().applicationContext)
                .load(strArtworkUrl)
                .into(it)
        }
    }

}
