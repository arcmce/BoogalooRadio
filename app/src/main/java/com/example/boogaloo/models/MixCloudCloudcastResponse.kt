package com.example.boogaloo.models

import java.util.*
import kotlin.collections.ArrayList


data class CloudcastResponse(
    val data: ArrayList<CloudcastData>
)

data class CloudcastData(
    val user: User,
    val key: String,
    val createdTime: Date,
    val name: String,
    val url: String,
    val pictures: Pictures
)

data class User(
    val url: String,
    val name: String,
    val pictures: Pictures
)