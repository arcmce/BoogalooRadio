package com.arcmce.boogaloo.network.repository

import com.arcmce.boogaloo.network.api.MixCloudApi
import com.arcmce.boogaloo.network.api.RadioApi
import com.arcmce.boogaloo.network.api.RetrofitInstance
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.MixCloudPlaylist
import com.arcmce.boogaloo.network.model.RadioInfo
import retrofit2.Call

class Repository() {
    private val radioApi: RadioApi = RetrofitInstance.createService("https://public.radio.co/", RadioApi::class.java)
    private val mixCloudApi: MixCloudApi = RetrofitInstance.createService("https://api.mixcloud.com/", MixCloudApi::class.java)

    fun getRadioInfo(): Call<RadioInfo> {
        return radioApi.getRadioInfo()
    }

    fun getPlaylist(): Call<MixCloudPlaylist> {
        return mixCloudApi.getPlaylist()
    }

    fun getCloudcast(key: String): Call<MixCloudCloudcast> {
        return mixCloudApi.getCloudcast(key)
    }
}
