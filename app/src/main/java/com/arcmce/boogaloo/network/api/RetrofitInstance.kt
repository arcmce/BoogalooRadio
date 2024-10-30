package com.arcmce.boogaloo.network.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val zonedDateTimeDeserializer = JsonDeserializer { json: JsonElement, _, _ ->
    ZonedDateTime.parse(json.asString, DateTimeFormatter.ISO_DATE_TIME)
}

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(ZonedDateTime::class.java, zonedDateTimeDeserializer)
    .create()

object RetrofitInstance {

    fun <T> createService(baseUrl: String, service: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(service)
    }
}
