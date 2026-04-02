package com.arcmce.boogaloo.ui.view

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arcmce.boogaloo.data.model.FavoriteArtist
import com.arcmce.boogaloo.data.model.FavoriteMix
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesView(
    favoritesViewModel: FavoritesViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    val favoriteArtists by favoritesViewModel.favoriteArtists.collectAsState()
    val favoriteMixes by favoritesViewModel.favoriteMixes.collectAsState()
    val scheduleItems by sharedViewModel.scheduleItems.collectAsState()
    val favoriteArtistNames by favoritesViewModel.favoriteArtistNames.collectAsState()

    val upcomingByArtist = remember(scheduleItems, favoriteArtistNames) {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val now = Date()
        scheduleItems
            .filter { item ->
                item.playlist.name in favoriteArtistNames &&
                runCatching { fmt.parse(item.end)!!.after(now) }.getOrDefault(false)
            }
            .sortedBy { it.start }
            .groupBy { it.playlist.name }
    }

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
            ) {
                Text("Upcoming", modifier = Modifier.padding(vertical = 12.dp))
            }
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
            ) {
                Text("Mixes", modifier = Modifier.padding(vertical = 12.dp))
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> UpcomingTab(
                    upcomingByArtist = upcomingByArtist,
                    favoriteArtists = favoriteArtists,
                    navController = navController,
                    onUnfavouriteArtist = { favoritesViewModel.toggleArtist(it) }
                )
                1 -> MixesTab(
                    favoriteMixes = favoriteMixes,
                    navController = navController,
                    onUnfavouriteMix = { favoritesViewModel.toggleMix(it) }
                )
            }
        }
    }
}

@Composable
private fun UpcomingTab(
    upcomingByArtist: Map<String, List<ScheduleItem>>,
    favoriteArtists: List<FavoriteArtist>,
    navController: NavController,
    onUnfavouriteArtist: (FavoriteArtist) -> Unit
) {
    var expandedArtists by remember { mutableStateOf(emptySet<String>()) }
    val noShowArtists = remember(favoriteArtists, upcomingByArtist) {
        favoriteArtists.filter { it.name !in upcomingByArtist.keys }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (favoriteArtists.isEmpty()) {
            item {
                Text(
                    text = "Star artists in the Schedule or Mixes tab to see upcoming shows here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }
        } else {
            upcomingByArtist.forEach { (artistName, items) ->
                val artist = favoriteArtists.find { it.name == artistName }
                val slug = artist?.slug ?: ""
                item(key = "header_$artistName") {
                    ArtistSectionHeader(
                        artistName = artistName,
                        slug = slug,
                        artist = artist,
                        navController = navController,
                        onUnfavouriteArtist = onUnfavouriteArtist
                    )
                    HorizontalDivider()
                }

                val isExpanded = artistName in expandedArtists
                val visibleItems = if (isExpanded) items else items.take(3)

                items(visibleItems, key = { "item_${it.eventId}" }) { item ->
                    FavoriteUpcomingRow(item = item)
                    HorizontalDivider()
                }

                if (items.size > 3) {
                    item(key = "expand_$artistName") {
                        val remaining = items.size - 3
                        TextButton(
                            onClick = {
                                expandedArtists = if (isExpanded) {
                                    expandedArtists - artistName
                                } else {
                                    expandedArtists + artistName
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isExpanded) "Show less"
                                else "$remaining more show${if (remaining > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            noShowArtists.forEach { artist ->
                item(key = "header_noshow_${artist.name}") {
                    ArtistSectionHeader(
                        artistName = artist.name,
                        slug = artist.slug,
                        artist = artist,
                        navController = navController,
                        onUnfavouriteArtist = onUnfavouriteArtist
                    )
                    HorizontalDivider()
                }
                item(key = "noshow_${artist.name}") {
                    Text(
                        text = "No upcoming shows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ArtistSectionHeader(
    artistName: String,
    slug: String,
    artist: FavoriteArtist?,
    navController: NavController,
    onUnfavouriteArtist: (FavoriteArtist) -> Unit
) {
    val navigateToMixes: () -> Unit = { navController.navigate("pastShow/$slug") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = artistName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (slug.isNotEmpty()) Modifier.clickable { navigateToMixes() } else Modifier
                )
        )
        if (slug.isNotEmpty()) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { navigateToMixes() }
            )
        }
        if (artist != null) {
            IconButton(
                onClick = { onUnfavouriteArtist(artist) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Remove from favourites",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MixesTab(
    favoriteMixes: List<FavoriteMix>,
    navController: NavController,
    onUnfavouriteMix: (FavoriteMix) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (favoriteMixes.isEmpty()) {
            item {
                Text(
                    text = "Long-hold a mix in the Mixes tab to save it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }
        } else {
            items(favoriteMixes, key = { it.url }) { mix ->
                FavoriteMixRow(
                    mix = mix,
                    onClick = if (mix.artistName.isNotEmpty()) {
                        { navController.navigate("pastShow/${mix.artistName}") }
                    } else null,
                    onUnfavourite = { onUnfavouriteMix(mix) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun FavoriteUpcomingRow(item: ScheduleItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArtworkImage(
            url = item.playlist.artwork,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatDateTimeSlot(item.start, item.end),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.playlist.artist,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
private fun FavoriteMixRow(mix: FavoriteMix, onClick: (() -> Unit)?, onUnfavourite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArtworkImage(
            url = mix.thumbnail,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mix.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = mix.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onClick != null) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onUnfavourite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Remove from favourites",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
