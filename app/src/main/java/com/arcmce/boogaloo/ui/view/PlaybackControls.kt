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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    var player: Player? = null

    // State to track whether the player is playing or not
    var isPlaying by remember { mutableStateOf(false) }

    val title by sharedViewModel.liveTitle.observeAsState()

    // TODO make into floating (looking) object
    // TODO actually play pause logo not button

    Log.d("PlaybackControls", "pre sessiontoken")
    val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    Log.d("PlaybackControls", "pre controllerFuture build")
    val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
    controllerFuture.addListener(
        {
            Log.d("PlaybackControls", "pre controllerfuture get")
            player = controllerFuture.get()

//            player
            player?.addListener(
                object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d("PlaybackControls", "update playback state callback")
                        updatePlaybackState(isPlaying)
                    }

                    private fun updatePlaybackState(playing: Boolean) {
                        isPlaying = playing
                    }
                }
            )
        },
        MoreExecutors.directExecutor()
    )



    // Top-level layout as a Row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
            .height(64.dp)
            .background(
                color = Color.White, // Solid white background
                shape = RoundedCornerShape(16.dp) // Adjust the corner radius as needed
            )
            .padding(8.dp) // Inner padding
//            .paint(
//                painter = painterResource(id = R.drawable.paper),
//                contentScale = ContentScale.Crop
//            )
        ,

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
//        Column(
//            modifier = modifier
//                .fillMaxHeight()
//                .weight(1f)
//                .padding(4.dp)
//                .padding(end = 64.dp)
//        ) {
//            // Marquee text that scrolls
//            Text(
//                text = "Boogaloo Radio: Live",
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier
//                    .weight(1f),
//                maxLines = 1, // Limit to one line
//            )
//            // Marquee text that scrolls
//            Text(
//                text = title ?: "Boogaloo Radio",
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier
//                    .weight(1f) // Make the text take up available space
////                    .padding(16.dp) // Add padding between text and button
//                    .basicMarquee(), // Add marquee scrolling
//                maxLines = 1, // Limit to one line
//                overflow = TextOverflow.Ellipsis // Fallback for no marquee support
//            )
//        }
        Text(
            text = title ?: "Boogaloo Radio",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f) // Make the text take up available space
                .padding(16.dp) // Add padding between text and button
                .basicMarquee(), // Add marquee scrolling
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis // Fallback for no marquee support
        )

        // Play/Pause button aligned to the right
        PlayPauseButton(
            isPlaying = isPlaying,
            onPlayPauseToggle = {
                if (isPlaying) {
                    player?.pause()
//                    audioFocusManager.abandonAudioFocus()
                    Log.d("PlaybackControls", "Stopping playback")
                    isPlaying = !isPlaying // Toggle playback state
                } else {
//                    if (audioFocusManager.requestAudioFocus()) {
                        player?.seekToDefaultPosition()
                        player?.play()
                        Log.d("PlaybackControls", "Starting playback")
                        isPlaying = !isPlaying // Toggle playback state
//                    }
                }
            }
        )
    }

}
//        // Play/Pause button
//        Button(onClick = {
//            if (isPlaying) {
////                stopPlaybackService(context)
//                player.pause()
//                Log.d("PlaybackControls", "Stopping playback")
//            } else {
////                startPlaybackService(context)
//                player.seekToDefaultPosition()
//                player.play()
//                Log.d("PlaybackControls", "Starting playback")
//            }
//            isPlaying = !isPlaying // Toggle playback state
//        }) {
//
//        }

//        Button(onClick = {
//            val mediaItem = MediaItem.Builder()
//                .setUri(AppConstants.RADIO_STREAM_URL)
//                .setMediaMetadata(
//                    MediaMetadata.Builder()
//                        .setTitle("")
//                        .setArtist(title)
////                            .setArtworkUri()
//                        .build()
//                )
//                .build()
//
//            // Update the player with the new media item
//            player.replaceMediaItem(0, mediaItem)
//        }) {
//            Text("update")
//        }
//
//        Button(onClick = {
//            player.seekToDefaultPosition()
//            player.play()
////            player.seekBack()
//        }) {
//            Text("play from live")
//        }
//
//        Button(onClick = {
//            Log.d("PlaybackControls", "${player.totalBufferedDuration}")
//            player.seekForward()
//            Log.d("PlaybackControls", "${player.totalBufferedDuration}")
////            player.seekBack()
//        }) {
//            Text("misc")
//        }





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