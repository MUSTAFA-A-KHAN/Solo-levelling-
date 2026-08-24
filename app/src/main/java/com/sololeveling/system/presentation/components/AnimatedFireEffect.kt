package com.sololeveling.system.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest

@Composable
fun AnimatedFireEffect(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("https://media1.giphy.com/media/cZ7rmKfFYOvYI/200.gif")
            .crossfade(false)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = modifier.fillMaxSize()
    )
}