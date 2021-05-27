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

        radioInfoRequest.getRadioInfo(::cloudcastRequestCallback)

        return inflater.inflate(R.layout.live_layout, container, false)

    }

    fun cloudcastRequestCallback(response: String) {

        val jsonResponse = JSONObject(response)
        val strCurrentTrack: String = jsonResponse.getJSONObject(
            "current_track")
            .get("title").toString()
        val strArtworkUrl: String = jsonResponse.getJSONObject(
            "current_track").
            get("artwork_url_large").toString()

        text_view_track.text = strCurrentTrack

        Glide.with(requireContext().applicationContext)
            .load(strArtworkUrl)
            .into(image_view)
    }

}
