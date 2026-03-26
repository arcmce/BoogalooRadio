package com.arcmce.boogaloo.ui.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.ui.viewmodel.LiveViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel

@Composable
fun LiveView(
    viewModel: LiveViewModel,
    sharedViewModel: SharedViewModel
) {

    val artworkUrl by viewModel.artworkUrl.collectAsState()
    val title by viewModel.title.observeAsState()
    val error by viewModel.error.collectAsState()
    val currentScheduleItem by viewModel.currentScheduleItem.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchSchedule() }

    val isDarkTheme by sharedViewModel.isDarkTheme.collectAsState()

    sharedViewModel.setArtworkUrl(artworkUrl)
    sharedViewModel.setLiveTitle(title)
    sharedViewModel.setCurrentScheduleItem(currentScheduleItem)

    val paperRes = if (isDarkTheme) R.drawable.paper_dark else R.drawable.paper_light

    var scheduleOpen by remember { mutableStateOf(false) }

    val topSpacerWeight by animateFloatAsState(
        targetValue = if (scheduleOpen) 0.01f else 1f,
        animationSpec = tween(400),
        label = "topSpacerWeight"
    )
    val bottomSpacerWeight by animateFloatAsState(
        targetValue = if (scheduleOpen) 0.01f else 1.5f,
        animationSpec = tween(400),
        label = "bottomSpacerWeight"
    )
    val panelWeight by animateFloatAsState(
        targetValue = if (scheduleOpen) 1f else 0.01f,
        animationSpec = tween(400),
        label = "panelWeight"
    )
    val panelAlpha by animateFloatAsState(
        targetValue = if (scheduleOpen) 1f else 0f,
        animationSpec = tween(400),
        label = "panelAlpha"
    )
    val panelSlide by animateFloatAsState(
        targetValue = if (scheduleOpen) 0f else 1f,
        animationSpec = tween(400),
        label = "panelSlide"
    )
    val imageFraction by animateFloatAsState(
        targetValue = if (scheduleOpen) 0.45f else 1f,
        animationSpec = tween(400),
        label = "imageFraction"
    )
    val imageHPadding by animateDpAsState(
        targetValue = if (scheduleOpen) 8.dp else 16.dp,
        animationSpec = tween(400),
        label = "imageHPadding"
    )
    val artworkBottomPadding by animateDpAsState(
        targetValue = if (scheduleOpen) 8.dp else 48.dp,
        animationSpec = tween(400),
        label = "artworkBottomPadding"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(topSpacerWeight))

        Box(
            modifier = Modifier
                .fillMaxWidth(imageFraction)
                .padding(horizontal = imageHPadding)
                .aspectRatio(1f)
                .shadow(elevation = 4.dp)
        ) {
            Image(
                painter = painterResource(id = paperRes),
                contentDescription = "Frame image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            ArtworkImage(
                url = artworkUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = artworkBottomPadding)
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }

        OutlinedButton(
            onClick = {
                scheduleOpen = !scheduleOpen
                sharedViewModel.setScheduleOpen(scheduleOpen)
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(if (scheduleOpen) "Close" else "Schedule")
        }

        Box(
            modifier = Modifier
                .weight(panelWeight)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = panelAlpha
                    translationY = size.height * panelSlide
                }
        ) {
            SchedulePanel(viewModel = viewModel, isDarkTheme = isDarkTheme, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.weight(bottomSpacerWeight))
    }
}
