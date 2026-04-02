package com.arcmce.boogaloo.ui.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.arcmce.boogaloo.BuildConfig
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.arcmce.boogaloo.data.model.FavoriteMix
import com.arcmce.boogaloo.ui.viewmodel.CloudcastCardItem
import com.arcmce.boogaloo.ui.viewmodel.CloudcastViewModel
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudcastView(
    viewModel: CloudcastViewModel,
    slug: String,
    sharedViewModel: SharedViewModel,
    favoritesViewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    if (BuildConfig.DEBUG) Log.d("CloudcastView", "CloudcastView composed: slug=$slug, instance=${System.identityHashCode(viewModel)}")

    LaunchedEffect(slug) {
        if (BuildConfig.DEBUG) Log.d("CloudcastView", "LaunchedEffect firing: slug=$slug")
        viewModel.loadCloudcast(slug)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        sharedViewModel.mixesTabTapped.collect {
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                if (gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0) {
                    onNavigateBack()
                } else {
                    coroutineScope.launch { gridState.animateScrollToItem(0) }
                }
            }
        }
    }

    val readyForSlug by viewModel.readyForSlug.collectAsState()

    if (BuildConfig.DEBUG) Log.d("CloudcastView", "readyForSlug=$readyForSlug slug=$slug lifecycleState=$lifecycleState showing=${readyForSlug == slug}")

    if (readyForSlug == slug && lifecycleState.isAtLeast(Lifecycle.State.STARTED)) {
        if (BuildConfig.DEBUG) Log.d("CloudcastView", "GRID IS VISIBLE for slug=$slug")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CloudcastVerticalGrid(viewModel, isResumed, gridState, favoritesViewModel, slug)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudcastVerticalGrid(
    viewModel: CloudcastViewModel,
    isResumed: Boolean,
    gridState: LazyGridState,
    favoritesViewModel: FavoritesViewModel,
    artistSlug: String
) {
    val cardItems by viewModel.cloudcastCardDataset.collectAsState(initial = emptyList())
    val favoriteMixUrls by favoritesViewModel.favoriteMixUrls.collectAsState()
    var selectedMixForMenu by remember { mutableStateOf<CloudcastCardItem?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState,
        userScrollEnabled = isResumed,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cardItems) { _, item ->
            CloudcastCardItemView(
                item = item,
                isResumed = isResumed,
                isFavorited = item.url in favoriteMixUrls,
                onLongPress = { if (isResumed) selectedMixForMenu = it }
            )
        }
    }

    selectedMixForMenu?.let { selected ->
        val isMixFavorited = selected.url in favoriteMixUrls
        ModalBottomSheet(
            onDismissRequest = { selectedMixForMenu = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = selected.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                TextButton(
                    onClick = {
                        favoritesViewModel.toggleMix(
                            FavoriteMix(
                                url = selected.url,
                                name = selected.name,
                                thumbnail = selected.thumbnail,
                                artistName = artistSlug
                            )
                        )
                        selectedMixForMenu = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isMixFavorited) "Unfavourite Mix" else "Favourite Mix")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CloudcastCardItemView(
    item: CloudcastCardItem,
    isResumed: Boolean,
    isFavorited: Boolean,
    onLongPress: (CloudcastCardItem) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isResumed) Modifier.combinedClickable(
                    onClick = {
                        if (BuildConfig.DEBUG) Log.d("CloudcastView", "onItemClicked " + item.name)
                        if (BuildConfig.DEBUG) Log.d("CloudcastView", "onItemClicked " + item.url)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        context.startActivity(intent)
                    },
                    onLongClick = { onLongPress(item) }
                ) else Modifier
            ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box {
            Column {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(8.dp)
                        .basicMarquee()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                if (isFavorited) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
