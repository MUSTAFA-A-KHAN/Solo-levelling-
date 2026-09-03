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
            .data("https://videos-preview-r41.rawpixel.com/video/2025/03/06/01jnmw6rf1cysgxg37dd5aky9j/transparent_gif.gif?v=1&dl=attachment%3B+filename%3D%22video-from-rawpixel-id-17167191-gif.gif%22")
            .crossfade(false)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "Animated fire effect",
        modifier = modifier.fillMaxSize()
    )
}