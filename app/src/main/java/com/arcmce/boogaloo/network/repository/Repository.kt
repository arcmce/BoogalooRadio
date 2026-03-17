package com.arcmce.boogaloo.network.repository

import com.arcmce.boogaloo.network.api.MixCloudApi
import com.arcmce.boogaloo.network.api.RadioApi
import com.arcmce.boogaloo.network.api.RetrofitInstance
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.MixCloudPlaylist
import com.arcmce.boogaloo.network.model.RadioInfo
import retrofit2.Response

class Repository() {
    private val radioApi: RadioApi = RetrofitInstance.createService("https://public.radio.co/", RadioApi::class.java)
    private val mixCloudApi: MixCloudApi = RetrofitInstance.createService("https://api.mixcloud.com/", MixCloudApi::class.java)

    suspend fun getRadioInfo(): Response<RadioInfo> = radioApi.getRadioInfo()

    suspend fun getPlaylist(): Response<MixCloudPlaylist> = mixCloudApi.getPlaylist()

    suspend fun getCloudcast(key: String): Response<MixCloudCloudcast> = mixCloudApi.getCloudcast(key)
}
