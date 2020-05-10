package com.arcmce.boogaloo.models

data class PlaylistResponse(
    val paging: Paging,
    val name: String,
    val data: ArrayList<PlaylistData>
)

data class Paging(
    val previous: String,
    val next: String
)

data class PlaylistData(
    val url: String,
    val owner: Owner,
    val name: String,
    val key: String,
    val slug: String
)

data class Owner(
    val url: String,
    val username: String,
    val name: String,
    val key: String,
    val pictures: Pictures
)
