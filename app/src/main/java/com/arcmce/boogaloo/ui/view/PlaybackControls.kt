package com.arcmce.boogaloo.ui.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

//    val audioFocusManager = remember { AudioFocusManager(context) }

//    var player: Player? = null
    var player by remember { mutableStateOf<Player?>(null) }


    // State to track whether the player is playing or not
//    var isPlaying by remember { mutableStateOf(false) }
    val isPlaying by sharedViewModel.isPlaying.collectAsState()

    val title by sharedViewModel.liveTitle.observeAsState()

    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()
                player?.addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Log.d("PlaybackControls", "onIsPlayingChanged ${player?.playbackState}")
                            sharedViewModel.setPlayingState(isPlaying)
                        }

//                        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//                            Log.d("PlaybackControls.Player.Listener", "onPlayWhenReadyChanged ${playWhenReady}")
//                        }
//
//                        override fun onEvents(player: Player, events: Player.Events) {
//                            Log.d("PlaybackControls.Player.Listener", "onPlayWhenReadyChanged ${events}")
//                        }
                    }
                )

                Log.d("PlaybackControls", "controllerfuture ${player == null}")
            },
            MoreExecutors.directExecutor()
        )
    }

    // Top-level layout as a Row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer, // Solid white background
                shape = RoundedCornerShape(10.dp) // Adjust the corner radius as needed
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title ?: "Boogaloo Radio",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(1f) // Make the text take up available space
                .padding(16.dp) // Add padding between text and button
                .basicMarquee(), // Add marquee scrolling
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis // Fallback for no marquee support
        )

        IconButton(onClick = {
            if (isPlaying) {
                Log.d("PlaybackControls", "${player == null}")
                player?.pause()
                Log.d("PlaybackControls", "Pausing playback")
            } else {
                Log.d("PlaybackControls", "${player == null}")
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

@Composable
fun PlayPauseButton(
    isPlaying: Boolean, // State to track if media is playing
    onPlayPauseToggle: () -> Unit // Lambda for click action to play/pause
) {
    // Use a Box or Button around the Image to make it clickable
    Box(modifier = Modifier
//        .size(80.dp)
//        .padding(16.dp)
        .clickable { onPlayPauseToggle() }) {
        // Conditionally choose the drawable based on isPlaying state
        val icon = if (isPlaying) {
            painterResource(id = R.drawable.ic_media_pause) // Replace with your pause button drawable
        } else {
            painterResource(id = R.drawable.ic_media_play) // Replace with your play button drawable
        }
//        IconButton() { }
        Image(
            painter = icon,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(48.dp), // Size can be adjusted as needed
            contentScale = ContentScale.Fit // Ensure proper scaling of the image
        )
    }
}

private fun startPlaybackService(context: Context) {
    val intent = Intent(context, PlaybackService::class.java)
    context.startService(intent)
    Log.d("PlaybackService", "playback service started")
}

private fun stopPlaybackService(context: Context) {
    val intent = Intent(context, PlaybackService::class.java)
    context.stopService(intent)
}