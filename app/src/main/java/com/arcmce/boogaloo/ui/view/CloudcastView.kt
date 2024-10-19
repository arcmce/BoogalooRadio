package com.arcmce.boogaloo.ui.view

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arcmce.boogaloo.ui.viewmodel.CloudcastCardItem
import com.arcmce.boogaloo.ui.viewmodel.CloudcastViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel

@Composable
fun CloudcastView(
    viewModel: CloudcastViewModel,
    sharedViewModel: SharedViewModel
) {

    viewModel.setCloudcast(sharedViewModel.getCloudcast())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CloudcastVerticalGrid(
            viewModel
        )
    }
}

@Composable
fun CloudcastVerticalGrid(
    viewModel: CloudcastViewModel
//    items: List<CatchUpCardItem>
) {


    val gridState = rememberLazyGridState()

    val cardItems by viewModel.cloudcastCardDataset.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize()
    ) {

        itemsIndexed(cardItems) { index, item ->

            CloudcastCardItemView(item)
        }
    }
}

@Composable
fun CloudcastCardItemView(item: CloudcastCardItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
            Log.d("CloudcastView", "onItemClicked " + item.name)
            Log.d("CloudcastView", "onItemClicked " + item.url)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(item.url)
            context.startActivity(intent)
        }
    ) {
        Column {
            // Load image using Coil
            AsyncImage(
                model = item.thumbnail,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
