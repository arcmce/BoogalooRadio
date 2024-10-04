package com.arcmce.boogaloo.network.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    fun <T> createService(baseUrl: String, service: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)
    }
}
