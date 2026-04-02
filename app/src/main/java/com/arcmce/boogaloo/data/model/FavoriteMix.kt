package com.arcmce.boogaloo.data.model

data class FavoriteMix(
    val url: String,        // unique Mixcloud URL; identity key
    val name: String,
    val thumbnail: String,
    val artistName: String, // Mixcloud slug; used for navigation
    val displayName: String? = null  // human-readable artist name for display
)
