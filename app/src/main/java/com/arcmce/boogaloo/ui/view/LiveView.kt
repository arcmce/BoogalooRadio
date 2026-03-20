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

    val isDarkTheme by sharedViewModel.isDarkTheme.collectAsState()

    sharedViewModel.setArtworkUrl(artworkUrl)
    sharedViewModel.setLiveTitle(title)

    val paperRes = if (isDarkTheme) R.drawable.paper_dark else R.drawable.paper_light


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
                    .padding(bottom = 48.dp)
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

        Spacer(modifier = Modifier.weight(1.5f))
    }
}
