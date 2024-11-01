package com.arcmce.boogaloo.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.Modifier
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

    val scheduleDays by viewModel.scheduleDays.collectAsState(emptyList())
    val daySchedule by viewModel.daySchedule.collectAsState(emptyMap())

    // Compute todayIndex after scheduleDays is populated
    val todayIndex = scheduleDays.indexOf(ZonedDateTime.now().toDayWithSuffix()).takeIf { it >= 0 } ?: 0

    // Initialize PagerState with default page count
    val pagerState = rememberPagerState(pageCount = { scheduleDays.size })

    // Ensure initial scroll to `todayIndex` after `scheduleDays` are available
    LaunchedEffect(scheduleDays) {
        if (todayIndex >= 0 && scheduleDays.isNotEmpty()) {
            pagerState.scrollToPage(todayIndex)
        }
    }

//    // Function to adjust tab titles to "Yesterday", "Today", and "Tomorrow"
//    fun formatTabTitle(day: String): String {
//        val today = ZonedDateTime.now().toDayWithSuffix()
//        return when (day) {
//            today -> "Today"
//            ZonedDateTime.now().minusDays(1).toDayWithSuffix() -> "Yesterday"
//            ZonedDateTime.now().plusDays(1).toDayWithSuffix() -> "Tomorrow"
//            else -> day
//        }
//    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (scheduleDays.isNotEmpty()) {
            val coroutineScope = rememberCoroutineScope()

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 8.dp
            ) {
                scheduleDays.forEachIndexed { index, day ->
                    Tab(
                        modifier = Modifier
                            .padding(
                                start = if (index == 0) 50.dp else 0.dp,
                                end = if (index == scheduleDays.lastIndex) 50.dp else 0.dp
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
        }

        if (scheduleDays.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val thisDaySchedule = daySchedule[scheduleDays[page]]

                // todo allow slight scroll beyond bottom for playback bar
                //todo scroll to current show in view
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 64.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
//                    TODO styling general
                    itemsIndexed(thisDaySchedule.orEmpty()) { index, item ->
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
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

                            Column {
                                Text("${item.start.toTimeFormat()} - ${item.end.toTimeFormat()}")
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