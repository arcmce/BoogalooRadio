package com.arcmce.boogaloo.network.api

import retrofit2.http.GET
import com.arcmce.boogaloo.network.model.RadioInfo
import com.arcmce.boogaloo.network.model.ScheduleResponse
import retrofit2.Response

interface RadioApi {
    @GET("stations/sb88c742f0/status")
    suspend fun getRadioInfo(): Response<RadioInfo>

    @GET("stations/sb88c742f0/embed/schedule")
    suspend fun getSchedule(): Response<ScheduleResponse>
}