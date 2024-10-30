package com.arcmce.boogaloo.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.ui.viewmodel.ColorCardItem
import com.arcmce.boogaloo.ui.viewmodel.ColorViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel

@Composable
fun ColorView(
    viewModel: ColorViewModel,
    sharedViewModel: SharedViewModel
) {
    val liveSchedule by sharedViewModel.liveSchedule.collectAsState()

    viewModel.setLiveSchedule(liveSchedule)

//    val paletteMap by viewModel.paletteMap.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColorLazyList(
            viewModel,
            sharedViewModel
        )
    }
}

@Composable
fun ColorLazyList(
    viewModel: ColorViewModel,
    sharedViewModel: SharedViewModel
) {

    val cardItems by viewModel.colorCardDataset.collectAsState(initial = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        itemsIndexed(cardItems) { index, item ->

            ColorCardItemView(item)
        }
    }
}

@Composable
fun ColorCardItemView(item: ColorCardItem) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
//            .wrapContentHeight()
//            .heightIn(min = 24.dp, max = 48.dp)
//            .height(64.dp)
            .background(
                color = item.color
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.thumbnail)
                .crossfade(true) // Enable crossfade animation
                .crossfade(500) // Optional: Adjust the duration (in milliseconds)
                .build(),
            contentDescription = "placeholder",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = painterResource(R.drawable.boogaloo_b),
            error = painterResource(R.drawable.boogaloo_b),
        )
    }
}
