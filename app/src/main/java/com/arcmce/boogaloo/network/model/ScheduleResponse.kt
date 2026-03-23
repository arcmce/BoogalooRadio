package com.arcmce.boogaloo.network.model

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("data") val data: List<ScheduleItem>
)

data class ScheduleItem(
    @SerializedName("start")     val start: String,
    @SerializedName("end")       val end: String,
    @SerializedName("event_id")  val eventId: Long,
    @SerializedName("playlist")  val playlist: SchedulePlaylist
)

data class SchedulePlaylist(
    @SerializedName("name")    val name: String,
    @SerializedName("colour")  val colour: String,
    @SerializedName("artist")  val artist: String,
    @SerializedName("artwork") val artwork: String
)
