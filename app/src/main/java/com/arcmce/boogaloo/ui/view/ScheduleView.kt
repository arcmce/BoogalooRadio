package com.arcmce.boogaloo.ui.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.ui.viewmodel.ScheduleViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.arcmce.boogaloo.util.AppConstants
import com.arcmce.boogaloo.util.toDayWithSuffix
import com.arcmce.boogaloo.util.toTimeFormat
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Composable
fun ScheduleView(
    viewModel: ScheduleViewModel,
    sharedViewModel: SharedViewModel
) {

    val radioSchedule by sharedViewModel.liveSchedule.collectAsState(initial = emptyList())
    viewModel.setRadioSchedule(radioSchedule)

    val scheduleByDate by viewModel.scheduleByDate.collectAsState()

    val uniqueDayNames by viewModel.uniqueDayNames.collectAsState()
    val formattedDayNames by viewModel.formattedDayNames.collectAsState()

    val currentDayIndex by viewModel.currentDayIndex.collectAsState()
    val currentShowIndex by viewModel.currentShowIndex.collectAsState()

    // Initialize PagerState with default page count
    val pagerState = rememberPagerState(pageCount = { formattedDayNames.size })

    // Use LazyListState to manage scroll position
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // scroll to current day tab
    LaunchedEffect(formattedDayNames, currentDayIndex) {
        if (currentDayIndex >= 0 && formattedDayNames.isNotEmpty()) {
            pagerState.scrollToPage(currentDayIndex)
        }
    }

    // Scroll to the current show item
    LaunchedEffect(currentShowIndex) {
        coroutineScope.launch {
            listState.animateScrollToItem(currentShowIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (formattedDayNames.isNotEmpty()) {

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 8.dp
            ) {
                formattedDayNames.forEachIndexed { index, day ->
                    Tab(
                        modifier = Modifier
                            .padding(
                                start = if (index == 0) 50.dp else 0.dp,
                                end = if (index == formattedDayNames.lastIndex) 50.dp else 0.dp
                            ),
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }},
//                        text = { Text(formatTabTitle(day)) }
                        text = { Text(day) }
                    )
                }
            }

            if (scheduleByDate.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val thisDaySchedule = scheduleByDate[uniqueDayNames[page]] ?: emptyList()
//                    thisDaySchedule = emptyList()

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 64.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(thisDaySchedule) { index, item ->
                            val isLiveSchedule = index == currentShowIndex && page == currentDayIndex
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = if (isLiveSchedule) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // Subtle highlight
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = RoundedCornerShape(8.dp) // Rounded corners for better visibility
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.playlist.artwork)
                                        .crossfade(true)
                                        .crossfade(500)
                                        .build(),
                                    contentDescription = item.playlist.name,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp),
                                    placeholder = painterResource(R.drawable.boogaloo_b),
                                    error = painterResource(R.drawable.boogaloo_b),
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${item.start.toTimeFormat()} - ${item.end.toTimeFormat()}")
                                        if (isLiveSchedule) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            FlashingDot()
                                        }
                                    }
                                    Text(
                                        text = item.playlist.title,
                                        fontWeight = FontWeight.Bold)
                                    Text(item.playlist.artist)

                                }
                            }
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FlashingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "live_dot"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Green.copy(alpha = alpha))
    )
}
