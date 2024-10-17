package com.arcmce.boogaloo.ui.view

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    sharedViewModel.setArtworkUrl(artworkUrl)
    sharedViewModel.setLiveTitle(title)

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//            .padding(bottom = 16.dp)
//            .shadow(elevation = 2.dp)
        ,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO play button here?
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .shadow(elevation = 4.dp)
        ) {
            // Frame image
            Image(
                painter = painterResource(id = R.drawable.paper),
                contentDescription = "Frame image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            artworkUrl?.let { url ->
                if (url.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .crossfade(500)
                            .build(),
                        placeholder = painterResource(id = R.drawable.ic_launcher_png),
                        error = painterResource(id = R.drawable.ic_launcher_png),
//                        model = url,
                        contentDescription = "Current show artwork",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(bottom = 48.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "No artwork available")
                }
            } ?:
            // TODO replace this text with placeholder image
            Text(text = title ?: "Boogaloo Radio", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.weight(1.5f))
    }
}
