package com.example.boogaloo.models

data class RecyclerviewModel(
    val data: ArrayList<RecyclerItem>
)

data class RecyclerItem(
    val name: String,
    var thumbnail: String,
    val slug: String
)
