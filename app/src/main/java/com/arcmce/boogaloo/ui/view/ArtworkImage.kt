package com.arcmce.boogaloo.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arcmce.boogaloo.R

@Composable
fun ArtworkImage(url: String?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    if (!url.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(500)
                .build(),
            contentDescription = "Artwork",
            placeholder = painterResource(R.drawable.boogaloo_b),
            error = painterResource(R.drawable.boogaloo_b),
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(R.drawable.boogaloo_b),
            contentDescription = "Artwork placeholder",
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
