package com.arcmce.boogaloo.network.api

import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.MixCloudPlaylist
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface MixCloudApi {
    @GET("BoogalooRadio/playlists/?limit=1000")
    suspend fun getPlaylist(): Response<MixCloudPlaylist>

    @GET("BoogalooRadio/playlists/{key}/cloudcasts/?limit=1000")
    suspend fun getCloudcast(@Path("key") key: String): Response<MixCloudCloudcast>
}
