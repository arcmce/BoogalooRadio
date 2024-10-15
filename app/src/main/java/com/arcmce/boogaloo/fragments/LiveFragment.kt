package com.arcmce.boogaloo.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.databinding.LiveLayoutBinding
import com.arcmce.boogaloo.network.RadioInfoRequest
import com.bumptech.glide.Glide
import org.json.JSONObject

class LiveFragment : androidx.fragment.app.Fragment() {

    private lateinit var radioInfoRequest: RadioInfoRequest
    private var _binding: LiveLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LVF", "onCreateView")

        _binding = LiveLayoutBinding.inflate(inflater, container, false)

        radioInfoRequest = RadioInfoRequest(requireContext().applicationContext)
        radioInfoRequest.getRadioInfo(::radioInfoRequestCallback)

        return binding.root
    }

    fun radioInfoRequestCallback(response: String) {

        val jsonResponse = JSONObject(response)
        val strArtworkUrl: String = jsonResponse.getJSONObject("current_track")
            .get("artwork_url_large").toString()

        updateThumbnail(strArtworkUrl)
    }


    fun updateThumbnail(strArtworkUrl: String) {

        Log.d("LVF", "updateThumbnail")

        binding.imageView.let { // Use binding to access the ImageView
            Glide.with(requireContext().applicationContext)
                .load(strArtworkUrl)
                .into(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding reference
    }
}
