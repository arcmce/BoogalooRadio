package com.arcmce.boogaloo.data.model

data class FavoriteArtist(
    val name: String,       // matches SchedulePlaylist.artist; identity key
    val slug: String,       // Mixcloud slug; empty if only known from schedule
    val thumbnail: String
)
