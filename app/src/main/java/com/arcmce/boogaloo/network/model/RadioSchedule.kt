package com.arcmce.boogaloo.network.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import java.util.Date

data class RadioSchedule(
    @SerializedName("data") val data: List<ScheduleItem>
)

data class ScheduleItem(
    val start: Date,
    val end: Date,
    val playlist: SchedulePlaylist
)

data class SchedulePlaylist(
    val name: String,
    val color: Color,
    val artist: String,
    val title: String,
    val artwork: String
)
