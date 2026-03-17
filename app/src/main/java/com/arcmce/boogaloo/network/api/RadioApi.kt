package com.arcmce.boogaloo.network.api

import retrofit2.http.GET
import com.arcmce.boogaloo.network.model.RadioInfo
import retrofit2.Response

interface RadioApi {
    @GET("stations/sb88c742f0/status")
    suspend fun getRadioInfo(): Response<RadioInfo>
}