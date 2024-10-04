package com.arcmce.boogaloo.network.model

import com.google.gson.annotations.SerializedName

data class RadioInfo(
    @SerializedName("current_track") val currentTrack: CurrentTrack,
    @SerializedName("history") val history: List<HistoryItem>,
    @SerializedName("logo_url") val logoUrl: String,
    @SerializedName("streaming_hostname") val streamingHostname: String
)

data class CurrentTrack(
    @SerializedName("title") val title: String,
    @SerializedName("artwork_url") val artworkUrl: String,
    @SerializedName("artwork_url_large") val artworkUrlLarge: String,
    @SerializedName("start_time") val startTime: String
)

data class HistoryItem(
    @SerializedName("title") val title: String
)