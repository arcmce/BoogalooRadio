package com.arcmce.boogaloo.ui.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.ui.viewmodel.CatchUpCardItem
import com.arcmce.boogaloo.ui.viewmodel.CatchUpViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel

@Composable
fun CatchUpView(
    viewModel: CatchUpViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.fetchPlaylist()
    }

//    val cardItems by viewModel.catchupCardDataset.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CatchUpVerticalGrid(
            viewModel,
            sharedViewModel,
            navController
//            cardItems
        )
    }
}

@Composable
fun CatchUpVerticalGrid(
    viewModel: CatchUpViewModel,
    sharedViewModel: SharedViewModel,
//    items: List<CatchUpCardItem>
    navController: NavController
) {

//    val viewModel: CatchUpViewModel = viewModel()

    val gridState = rememberLazyGridState()

    val cardItems by viewModel.catchupCardDataset.collectAsState(initial = emptyList())

    // Observe changes to visible items using snapshotFlow
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                Log.d("CatchUpView", "Visible items: $visibleItems")

                visibleItems.forEach { itemInfo ->
                    val item = cardItems.getOrNull(itemInfo.index)
                    if (item != null) {
                        viewModel.fetchCloudcastData(item.slug)
                        Log.d("CatchUpView", "Fetching thumbnail for index: ${itemInfo.index}")
                    }
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize()
    ) {

        itemsIndexed(cardItems) { index, item ->

            CardItemView(
                item,
                viewModel,
                sharedViewModel,
                navController
            )
        }
    }
}

@Composable
fun CardItemView(
    item: CatchUpCardItem,
    viewModel: CatchUpViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController) {

//    val viewModel: CatchUpViewModel = viewModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
            val cloudcast = viewModel.getCloudcast(item.slug)
            sharedViewModel.setCloudcast(cloudcast)
            navController.navigate("pastShow/${item.slug}") {
                // Pass cloudcastData as a parameter or using a shared ViewModel
            }
        }
    ) {
        Column {
            // Load image using Coil
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.thumbnail)
                    .crossfade(true) // Enable crossfade animation
                    .crossfade(500) // Optional: Adjust the duration (in milliseconds)
                    .build(),
//                item.thumbnail,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = painterResource(R.drawable.ic_launcher_png),
                error = painterResource(R.drawable.ic_launcher_png),
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}