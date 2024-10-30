package com.arcmce.boogaloo.ui.view

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.playback.PlaybackService
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel
import com.arcmce.boogaloo.util.AppConstants
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun PlaybackControls(context: Context, sharedViewModel: SharedViewModel, modifier: Modifier = Modifier) {

    var player by remember { mutableStateOf<Player?>(null) }

    val isPlaying by sharedViewModel.isPlaying.collectAsState()

    val title by sharedViewModel.liveTitle.collectAsState()

    val artist by sharedViewModel.liveArtist.collectAsState()

    val artworkColorSwatch by sharedViewModel.artworkColorSwatch.collectAsState()

    val artworkUrl by sharedViewModel.liveArtworkUrl.collectAsState()

    val adjustedColor = artworkColorSwatch?.hsl?.let { hsl ->
        hsl[1] = (hsl[1] * 0.8f).coerceIn(0f, 1f)

        hsl[2] = (hsl[2] * 0.8f).coerceIn(0f, 1f)

        // Convert back to Color
        Color(ColorUtils.HSLToColor(hsl))
    } ?: Color.Gray // Fallback to gray if swatch is null

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
            .wrapContentHeight()
            .heightIn(min = 24.dp, max = 48.dp)
//            .height(64.dp)
            .background(
//                color = MaterialTheme.colorScheme.primaryContainer,
                color = Color(artworkColorSwatch?.rgb ?: Color.Gray.toArgb()),
//                color = adjustedColor,
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
                Image(
                    painter = painterResource(id = R.drawable.boogaloo_b),
                    contentDescription = "Current show artwork placeholder",
                    modifier =  Modifier
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
            }
        } ?:
        Image(
            painter = painterResource(id = R.drawable.boogaloo_b),
            contentDescription = "Current show artwork placeholder",
            modifier =  Modifier
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

        Column(
            modifier = Modifier
                .weight(1f) // Take up available space within the Row
                .padding(horizontal = 16.dp) // Add padding between text and button
        ) {
            Text(
                text = title ?: AppConstants.RADIO_TITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb()),
                modifier = Modifier
                    .basicMarquee(),
                fontWeight = FontWeight.Bold,
                maxLines = 1, // Limit to one line
                overflow = TextOverflow.Ellipsis // Fallback for no marquee support
            )
            Text(
                text = artist ?: AppConstants.DEFAULT_ARTIST,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(artworkColorSwatch?.bodyTextColor ?: Color.White.toArgb()),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

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
