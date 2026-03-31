package com.arcmce.boogaloo.ui.view

import android.util.Log
import com.arcmce.boogaloo.BuildConfig
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.arcmce.boogaloo.data.model.FavoriteArtist
import com.arcmce.boogaloo.ui.viewmodel.CatchUpCardItem
import com.arcmce.boogaloo.ui.viewmodel.CatchUpViewModel
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import coil.request.ImageRequest
import com.arcmce.boogaloo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchUpView(
    viewModel: CatchUpViewModel,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    favoritesViewModel: FavoritesViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.fetchPlaylist()
    }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        sharedViewModel.catchUpScrollToTop.collect {
            coroutineScope.launch { gridState.animateScrollToItem(0) }
        }
    }

    LaunchedEffect(Unit) {
        sharedViewModel.mixesTabTapped.collect {
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                if (gridState.firstVisibleItemIndex != 0 || gridState.firstVisibleItemScrollOffset != 0) {
                    coroutineScope.launch { gridState.animateScrollToItem(0) }
                }
            }
        }
    }

    val favoriteArtistNames by favoritesViewModel.favoriteArtistNames.collectAsState()
    var selectedArtistForMenu by remember { mutableStateOf<CatchUpCardItem?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CatchUpVerticalGrid(
            viewModel,
            navController,
            gridState,
            favoriteArtistNames,
            onLongPress = { selectedArtistForMenu = it }
        )
    }

    selectedArtistForMenu?.let { selected ->
        val isArtistFavorited = selected.name in favoriteArtistNames
        ModalBottomSheet(
            onDismissRequest = { selectedArtistForMenu = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(selected.name, style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = {
                        favoritesViewModel.toggleArtist(
                            FavoriteArtist(
                                name = selected.name,
                                slug = selected.slug,
                                thumbnail = selected.thumbnail
                            )
                        )
                        selectedArtistForMenu = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArtistFavorited) "Unfavourite Artist" else "Favourite Artist")
                }
            }
        }
    }
}

@Composable
fun CatchUpVerticalGrid(
    viewModel: CatchUpViewModel,
    navController: NavController,
    gridState: LazyGridState,
    favoriteArtistNames: Set<String>,
    onLongPress: (CatchUpCardItem) -> Unit
) {
    val cardItems by viewModel.catchupCardDataset.collectAsState(initial = emptyList())

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (BuildConfig.DEBUG) Log.d("CatchUpView", "Visible items: $visibleItems")
                visibleItems.forEach { itemInfo ->
                    val item = cardItems.getOrNull(itemInfo.index)
                    if (item != null) {
                        viewModel.fetchCloudcastData(item.slug)
                        if (BuildConfig.DEBUG) Log.d("CatchUpView", "Fetching thumbnail for index: ${itemInfo.index}")
                    }
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cardItems) { _, item ->
            CardItemView(
                item = item,
                navController = navController,
                isFavorited = item.name in favoriteArtistNames,
                onLongPress = onLongPress
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardItemView(
    item: CatchUpCardItem,
    navController: NavController,
    isFavorited: Boolean,
    onLongPress: (CatchUpCardItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (BuildConfig.DEBUG) Log.d("CatchUpView", "card tapped: navigating to pastShow/${item.slug}")
                    navController.navigate("pastShow/${item.slug}")
                },
                onLongClick = { onLongPress(item) }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box {
            Column {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.thumbnail)
                        .crossfade(true)
                        .crossfade(500)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    placeholder = painterResource(R.drawable.boogaloo_b),
                    error = painterResource(R.drawable.boogaloo_b),
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Icon(
                imageVector = if (isFavorited) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (isFavorited) MaterialTheme.colorScheme.primary
                       else Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
            )
        }
    }
}
