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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arcmce.boogaloo.data.model.FavoriteMix
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.ui.viewmodel.FavoritesViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
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

    val upcomingItems = remember(scheduleItems, favoriteArtistNames) {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val now = Date()
        scheduleItems
            .filter { item ->
                item.playlist.artist in favoriteArtistNames &&
                runCatching { fmt.parse(item.end)!!.after(now) }.getOrDefault(false)
            }
            .sortedBy { it.start }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (upcomingItems.isEmpty()) {
            item {
                Text(
                    text = if (favoriteArtistNames.isEmpty())
                        "Star artists in the Schedule or Mixes tab to see upcoming shows here."
                    else
                        "No upcoming shows for your favourite artists.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        } else {
            items(upcomingItems, key = { it.eventId }) { item ->
                val slug = favoriteArtists.find { it.name == item.playlist.artist }?.slug ?: ""
                FavoriteUpcomingRow(
                    item = item,
                    onClick = if (slug.isNotEmpty()) {
                        { navController.navigate("pastShow/$slug") }
                    } else null
                )
                HorizontalDivider()
            }
        }

        item {
            Text(
                text = "Mixes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }

        if (favoriteMixes.isEmpty()) {
            item {
                Text(
                    text = "Long-hold a mix in the Mixes tab to save it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        } else {
            items(favoriteMixes, key = { it.url }) { mix ->
                FavoriteMixRow(mix)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun FavoriteUpcomingRow(item: ScheduleItem, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
                text = formatTimeSlot(item.start, item.end),
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
            Text(
                text = item.playlist.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FavoriteMixRow(mix: FavoriteMix) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
    }
}
