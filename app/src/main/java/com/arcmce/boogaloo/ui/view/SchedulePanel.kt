package com.arcmce.boogaloo.ui.view

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.ui.viewmodel.LiveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun SchedulePanel(
    viewModel: LiveViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val allItems by viewModel.scheduleItems.collectAsState()
    val isLoading by viewModel.scheduleLoading.collectAsState()
    val error by viewModel.scheduleError.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchSchedule() }

    val dayDateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    val todayKey = remember { dayDateFmt.format(Date()) }
    val yesterdayKey = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time.let { dayDateFmt.format(it) }
    }
    val tomorrowKey = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time.let { dayDateFmt.format(it) }
    }

    val days = remember(allItems) {
        allItems
            .groupBy { it.start.take(10) }
            .entries.sortedBy { it.key }
            .map { (key, items) ->
                val label = when (key) {
                    yesterdayKey -> "Yesterday"
                    todayKey     -> "Today"
                    tomorrowKey  -> "Tomorrow"
                    else         -> runCatching {
                        SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(dayDateFmt.parse(key)!!)
                    }.getOrDefault(key)
                }
                Triple(key, label, items)
            }
    }

    val todayIndex = remember(days) { days.indexOfFirst { it.first == todayKey }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = todayIndex) { days.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier) {
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null -> Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )

            days.isNotEmpty() -> {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 8.dp
                ) {
                    days.forEachIndexed { i, (_, label, _) ->
                        Tab(
                            selected = pagerState.currentPage == i,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(i) } }
                        ) {
                            Text(label, modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val dayItems = days.getOrNull(page)?.third ?: emptyList()
                    val isCurrentDay = days.getOrNull(page)?.first == todayKey
                    val currentShowIndex = remember(dayItems) { findCurrentShowIndex(dayItems) }
                    val listState = rememberLazyListState()

                    LaunchedEffect(currentShowIndex) {
                        if (isCurrentDay && currentShowIndex >= 0) {
                            listState.animateScrollToItem(currentShowIndex)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        itemsIndexed(dayItems, key = { _, item -> item.eventId }) { index, item ->
                            val isLive = isCurrentDay && index == currentShowIndex
                            ScheduleItemRow(item, isLive, isDarkTheme)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemRow(item: ScheduleItem, isLive: Boolean, isDarkTheme: Boolean) {
    val accentColor = rememberArtworkColor(item.playlist.artwork, isDarkTheme)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                if (isLive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))

        ArtworkImage(
            url = item.playlist.artwork,
            modifier = Modifier
                .size(72.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimeSlot(item.start, item.end),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLive) {
                    Box(modifier = Modifier.padding(start = 6.dp)) { FlashingDot() }
                }
            }
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
private fun FlashingDot() {
    val transition = rememberInfiniteTransition(label = "live_dot")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Green.copy(alpha = alpha))
    )
}

@Composable
private fun rememberArtworkColor(url: String, isDarkTheme: Boolean): Color {
    val context = LocalContext.current
    var color by remember(url) { mutableStateOf<Color?>(null) }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val bitmap = (loader.execute(request) as? SuccessResult)?.drawable
                ?.let { it as? BitmapDrawable }?.bitmap
            bitmap?.let {
                val palette = Palette.from(it).generate()
                val swatch = if (isDarkTheme) {
                    palette.darkVibrantSwatch ?: palette.darkMutedSwatch
                } else {
                    palette.lightVibrantSwatch ?: palette.lightMutedSwatch
                        ?: palette.mutedSwatch ?: palette.vibrantSwatch
                }
                color = swatch?.let { s -> Color(s.rgb) }
            }
        }
    }

    return color ?: Color.Gray
}

private fun findCurrentShowIndex(items: List<ScheduleItem>): Int {
    val now = Date()
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    return items.indexOfFirst { item ->
        runCatching {
            val start = fmt.parse(item.start)!!
            val end = fmt.parse(item.end)!!
            now.after(start) && now.before(end)
        }.getOrDefault(false)
    }
}

private fun formatTimeSlot(start: String, end: String): String {
    val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    val outFmt = SimpleDateFormat("HH:mm", Locale.US).also { it.timeZone = TimeZone.getDefault() }
    return runCatching {
        "${outFmt.format(inFmt.parse(start)!!)} – ${outFmt.format(inFmt.parse(end)!!)}"
    }.getOrDefault("$start – $end")
}
