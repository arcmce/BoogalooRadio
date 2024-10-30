package com.arcmce.boogaloo.network.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime

data class RadioSchedule(
    @SerializedName("data") val data: List<ScheduleItem>
)

data class ScheduleItem(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val playlist: SchedulePlaylist
)

data class SchedulePlaylist(
    val name: String,
    val color: Color,
    val artist: String,
    val title: String,
    val artwork: String
)
