package com.arcmce.boogaloo.ui.view

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun PlaybackControls(context: Context, sharedViewModel: SharedViewModel, modifier: Modifier = Modifier) {

    var player by remember { mutableStateOf<Player?>(null) }

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
                player?.addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Log.d("PlaybackControls", "onIsPlayingChanged ${player?.playbackState}")
                            if (player?.playbackState == Player.STATE_READY) {
                                sharedViewModel.setPlayingState(isPlaying)
                            }
                        }
                    }
                )

                Log.d("PlaybackControls", "controllerfuture ${player == null}")
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
            .height(64.dp)
            .background(
//                color = MaterialTheme.colorScheme.primaryContainer,
                color = Color(artworkColorSwatch?.rgb ?: Color.Gray.toArgb()),
                shape = RoundedCornerShape(10.dp) // Adjust the corner radius as needed
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        artworkUrl?.let { url ->
            if (url.isNotEmpty()) {
                AsyncImage(
                    model = url,
                    contentDescription = "Current show artwork",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 10.dp,
                                bottomStart = 10.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            )
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "No artwork available")
            }
        }
        Text(
            text = title ?: "Boogaloo Radio",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb()),
            modifier = Modifier
                .weight(1f) // Make the text take up available space
                .padding(16.dp) // Add padding between text and button
                .basicMarquee(), // Add marquee scrolling
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis // Fallback for no marquee support
        )

        // TODO can this be improved?
        IconButton(onClick = {
            if (isPlaying) {
                player?.pause()
                Log.d("PlaybackControls", "Pausing playback")
            } else {
                player?.seekToDefaultPosition()
                player?.play()
                Log.d("PlaybackControls", "Starting playback")
            }
        }) {
            if (isPlaying) {
                Log.d("PlaybackControls", "is playing, icon changing to play")
                Icon(
                    painter = painterResource(id = R.drawable.ic_media_pause),
                    contentDescription = "Pause button",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Log.d("PlaybackControls", "is paused, icon changing to pause")
                Icon(
                    painter = painterResource(id = R.drawable.ic_media_play),
                    contentDescription = "Play button",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
