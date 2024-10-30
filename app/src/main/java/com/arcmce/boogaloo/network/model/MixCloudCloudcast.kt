package com.arcmce.boogaloo.network.model

import java.time.ZonedDateTime


data class MixCloudCloudcast(
    val data: ArrayList<CloudcastData>
)

data class CloudcastData(
    val user: User,
    val key: String,
    val createdTime: ZonedDateTime,
    val name: String,
    val url: String,
    val pictures: Pictures
)

data class User(
    val url: String,
    val name: String,
    val pictures: Pictures
)