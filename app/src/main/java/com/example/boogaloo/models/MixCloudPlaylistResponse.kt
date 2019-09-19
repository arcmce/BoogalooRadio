package com.example.boogaloo.models

data class PlaylistResponse(
    val paging: Paging,
    val name: String,
    val data: ArrayList<Data>
)

data class Paging(
    val previous: String,
    val next: String
)

data class Data(
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

data class Pictures(
    val thumbnail: String,
    val small: String,
    val medium: String,
    val mediumMobile: String,
    val large: String,
    val extraLarge: String
)