package com.arcmce.boogaloo.ui.viewmodel

import androidx.lifecycle.*
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class CloudcastCardItem(
    val name: String,
    var thumbnail: String,
    val url: String,
//    val url: ArrayList<String>
)

class CloudcastViewModel() : ViewModel() {

    private val _cloudcastCardDataset = MutableStateFlow<List<CloudcastCardItem>>(emptyList())
    val cloudcastCardDataset: StateFlow<List<CloudcastCardItem>> get() = _cloudcastCardDataset

    private val _cloudcast = MutableStateFlow<MixCloudCloudcast?>(null)
    val cloudcast: StateFlow<MixCloudCloudcast?> = _cloudcast

    fun setCloudcast(cloudcast: MixCloudCloudcast?) {
        _cloudcast.value = cloudcast

        val dataset = cloudcast?.data?.map { playlist ->
            CloudcastCardItem(
                name = playlist.name,
                thumbnail = playlist.pictures.large,
                url = playlist.url
            )
        } ?: emptyList()

        _cloudcastCardDataset.value = dataset
    }
}
