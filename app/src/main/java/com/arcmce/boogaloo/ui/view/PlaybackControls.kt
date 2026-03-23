package com.arcmce.boogaloo.ui.view

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.arcmce.boogaloo.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun PlaybackControls(context: Context, sharedViewModel: SharedViewModel, modifier: Modifier = Modifier) {

    var player by remember { mutableStateOf<Player?>(null) }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "barScale"
    )

    val isPlaying by sharedViewModel.isPlaying.collectAsState()

    val title by sharedViewModel.liveTitle.observeAsState()

    val artworkColorSwatch by sharedViewModel.artworkColorSwatch.collectAsState()

    val artworkUrl by sharedViewModel.artworkUrl.collectAsState()

    DisposableEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()

                val playing = player?.isPlaying == true
                sharedViewModel.setPlayingState(playing)

                player?.addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (BuildConfig.DEBUG) Log.d("PlaybackControls", "onIsPlayingChanged ${player?.playbackState}")
                            if (player?.playbackState == Player.STATE_READY) {
                                sharedViewModel.setPlayingState(isPlaying)
                            }
                        }
                    }
                )

                if (BuildConfig.DEBUG) Log.d("PlaybackControls", "controllerfuture ${player == null}")
            },
            MoreExecutors.directExecutor()
        )

        onDispose {
            player?.release()
            player = null

        }
    }

    // Top-level layout as a Row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
            .wrapContentHeight()
            .heightIn(min = 24.dp, max = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .background(
                color = Color(artworkColorSwatch?.rgb ?: Color.Gray.toArgb()),
                shape = RoundedCornerShape(10.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ArtworkImage(
            url = artworkUrl,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        bottomStart = 10.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        )

        val textColor = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb())
        val titleParts = title?.split(" - ", limit = 2)
        val showName = titleParts?.getOrNull(0) ?: "Boogaloo Radio"
        val hostName = titleParts?.getOrNull(1)

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = showName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            if (hostName != null) {
                Text(
                    text = hostName,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }
        }

        AnimatedVisibility(visible = isPlaying) {
            EqualizerBars(
                color = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb()),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        IconButton(onClick = {
            if (isPlaying) {
                player?.pause()
                if (BuildConfig.DEBUG) Log.d("PlaybackControls", "Pausing playback")
            } else {
                player?.seekToDefaultPosition()
                player?.play()
                if (BuildConfig.DEBUG) Log.d("PlaybackControls", "Starting playback")
            }
        }) {
            val iconRes = if (isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
            val contentDescription = if (isPlaying) "Pause button" else "Play button"

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb())
            )
        }
    }
}

@Composable
fun EqualizerBars(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by transition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction)
                    .background(color)
            )
        }
    }
}
