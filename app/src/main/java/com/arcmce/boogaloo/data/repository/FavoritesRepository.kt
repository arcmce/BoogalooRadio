package com.arcmce.boogaloo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arcmce.boogaloo.data.model.FavoriteArtist
import com.arcmce.boogaloo.data.model.FavoriteMix
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

class FavoritesRepository(private val context: Context) {

    private val gson = Gson()
    private val ARTISTS_KEY = stringPreferencesKey("favorite_artists")
    private val MIXES_KEY = stringPreferencesKey("favorite_mixes")

    val favoriteArtists: Flow<List<FavoriteArtist>> = context.favoritesDataStore.data
        .map { prefs ->
            gson.fromJson<List<FavoriteArtist>>(
                prefs[ARTISTS_KEY] ?: return@map emptyList(),
                object : TypeToken<List<FavoriteArtist>>() {}.type
            ) ?: emptyList()
        }

    val favoriteMixes: Flow<List<FavoriteMix>> = context.favoritesDataStore.data
        .map { prefs ->
            gson.fromJson<List<FavoriteMix>>(
                prefs[MIXES_KEY] ?: return@map emptyList(),
                object : TypeToken<List<FavoriteMix>>() {}.type
            ) ?: emptyList()
        }

    suspend fun toggleArtist(artist: FavoriteArtist) {
        context.favoritesDataStore.edit { prefs ->
            val current: MutableList<FavoriteArtist> = gson.fromJson<List<FavoriteArtist>>(
                prefs[ARTISTS_KEY] ?: "[]",
                object : TypeToken<List<FavoriteArtist>>() {}.type
            )?.toMutableList() ?: mutableListOf()

            val idx = current.indexOfFirst { it.name == artist.name }
            if (idx >= 0) {
                val stored = current[idx]
                if (artist.slug.isNotEmpty() && stored.slug.isEmpty()) {
                    current[idx] = artist  // upgrade with slug from CatchUpView
                } else {
                    current.removeAt(idx)  // unfavourite
                }
            } else {
                current.add(artist)
            }
            prefs[ARTISTS_KEY] = gson.toJson(current)
        }
    }

    suspend fun toggleMix(mix: FavoriteMix) {
        context.favoritesDataStore.edit { prefs ->
            val current: MutableList<FavoriteMix> = gson.fromJson<List<FavoriteMix>>(
                prefs[MIXES_KEY] ?: "[]",
                object : TypeToken<List<FavoriteMix>>() {}.type
            )?.toMutableList() ?: mutableListOf()

            val idx = current.indexOfFirst { it.url == mix.url }
            if (idx >= 0) current.removeAt(idx) else current.add(mix)
            prefs[MIXES_KEY] = gson.toJson(current)
        }
    }
}
