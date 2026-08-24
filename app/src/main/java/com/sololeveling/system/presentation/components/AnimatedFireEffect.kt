package com.sololeveling.system.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.sololeveling.system.R

@Composable
fun AnimatedFireEffect(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(R.drawable.blue_flame)
            .crossfade(false)
            .build(),
        imageLoader = coil.ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build(),
        contentDescription = null,
        modifier = modifier.fillMaxSize()
    )
}