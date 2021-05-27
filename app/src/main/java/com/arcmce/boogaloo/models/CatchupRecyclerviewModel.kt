package com.arcmce.boogaloo.models

data class RecyclerviewModel(
    val data: ArrayList<CatchupRecyclerItem>
)

data class CatchupRecyclerItem(
    val name: String,
    var thumbnail: String,
    val slug: String,
    val url: ArrayList<String>
)
