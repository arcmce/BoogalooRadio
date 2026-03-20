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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.arcmce.boogaloo.ui.viewmodel.CloudcastCardItem
import com.arcmce.boogaloo.ui.viewmodel.CloudcastViewModel

@Composable
fun CloudcastView(
    viewModel: CloudcastViewModel,
    slug: String
) {
    Log.d("CloudcastView", "CloudcastView composed: slug=$slug, instance=${System.identityHashCode(viewModel)}")

    LaunchedEffect(slug) {
        Log.d("CloudcastView", "LaunchedEffect firing: slug=$slug")
        viewModel.loadCloudcast(slug)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    val readyForSlug by viewModel.readyForSlug.collectAsState()

    Log.d("CloudcastView", "readyForSlug=$readyForSlug slug=$slug lifecycleState=$lifecycleState showing=${readyForSlug == slug}")

    if (readyForSlug == slug && lifecycleState.isAtLeast(Lifecycle.State.STARTED)) {
        Log.d("CloudcastView", "GRID IS VISIBLE for slug=$slug")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CloudcastVerticalGrid(viewModel, isResumed)
        }
    }
}

@Composable
fun CloudcastVerticalGrid(
    viewModel: CloudcastViewModel,
    isResumed: Boolean
) {
    val gridState = rememberLazyGridState()

    val cardItems by viewModel.cloudcastCardDataset.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState,
        userScrollEnabled = isResumed,
        modifier = Modifier.fillMaxSize()
    ) {

        itemsIndexed(cardItems) { index, item ->

            CloudcastCardItemView(item, isResumed)
        }
    }
}

@Composable
fun CloudcastCardItemView(item: CloudcastCardItem, isResumed: Boolean) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(if (isResumed) Modifier.clickable {
                Log.d("CloudcastView", "onItemClicked " + item.name)
                Log.d("CloudcastView", "onItemClicked " + item.url)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(item.url)
                context.startActivity(intent)
            } else Modifier),
        elevation = CardDefaults.cardElevation(4.dp),
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
