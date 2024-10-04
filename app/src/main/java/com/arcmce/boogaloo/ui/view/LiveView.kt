package com.arcmce.boogaloo.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.ui.viewmodel.LiveViewModel
import com.arcmce.boogaloo.ui.viewmodel.SharedViewModel

@Composable
fun LiveView(
    viewModel: LiveViewModel,
    sharedViewModel: SharedViewModel
) {

    // Observe the artwork URL from the ViewModel
    val artworkUrl by viewModel.artworkUrl.observeAsState()
    val title by viewModel.title.observeAsState()

    sharedViewModel.setLiveTitle(title)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(vertical = 128.dp)
            .shadow(elevation = 2.dp)
//            .padding(bottom = 64.dp)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display image if artworkUrl is available
        // TODO stretch to screen
        // TODO play button here?
//        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
//                .aspectRatio(1f) // Maintain a square aspect ratio
        ) {
            // Frame image
            Image(
                painter = painterResource(id = R.drawable.paper),
                contentDescription = "Frame image",
                modifier = Modifier
                    .fillMaxSize()

//                    .padding(top = 64.dp)
//                    .padding(bottom = 0.dp)
//                    .padding(16.dp)
                    ,
                contentScale = ContentScale.Crop
            )

            artworkUrl?.let { url ->
                if (url.isNotEmpty()) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Current show artwork",
                        modifier = Modifier
                            .fillMaxWidth() // Fill the width of the screen
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp)
//                            .padding(bottom = 48.dp)
                            .aspectRatio(1f),
                        //                    modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "No artwork available")
                }
            } ?:
            // TODO replace this text with placeholder image
            Text(text = title ?: "Boogaloo Radio", style = MaterialTheme.typography.bodyLarge)
        }
    }
}