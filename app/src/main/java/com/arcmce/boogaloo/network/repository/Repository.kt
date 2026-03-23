package com.arcmce.boogaloo.network.repository

import com.arcmce.boogaloo.network.api.MixCloudApi
import com.arcmce.boogaloo.network.api.RadioApi
import com.arcmce.boogaloo.network.api.RetrofitInstance
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.MixCloudPlaylist
import com.arcmce.boogaloo.network.model.RadioInfo
import com.arcmce.boogaloo.network.model.ScheduleResponse
import retrofit2.Response

class Repository() {
    private val radioApi: RadioApi = RetrofitInstance.createService("https://public.radio.co/", RadioApi::class.java)
    private val mixCloudApi: MixCloudApi = RetrofitInstance.createService("https://api.mixcloud.com/", MixCloudApi::class.java)

    private data class CacheEntry(val data: MixCloudCloudcast, val timestamp: Long)
    private val cloudcastCache = mutableMapOf<String, CacheEntry>()
    private val cacheTtlMs = 2 * 60 * 1000L

    suspend fun getRadioInfo(): Response<RadioInfo> = radioApi.getRadioInfo()

    suspend fun getSchedule(): Response<ScheduleResponse> = radioApi.getSchedule()

    suspend fun getPlaylist(): Response<MixCloudPlaylist> = mixCloudApi.getPlaylist()

    suspend fun getCloudcast(key: String): Response<MixCloudCloudcast> {
        val cached = cloudcastCache[key]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < cacheTtlMs) {
            return Response.success(cached.data)
        }
        return mixCloudApi.getCloudcast(key).also { response ->
            response.body()?.let { cloudcastCache[key] = CacheEntry(it, System.currentTimeMillis()) }
        }
    }
}
