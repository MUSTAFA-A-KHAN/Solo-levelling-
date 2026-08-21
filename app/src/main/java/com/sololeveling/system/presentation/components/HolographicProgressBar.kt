package com.sololeveling.system.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sololeveling.system.presentation.theme.SystemNeonBlue

@Composable
fun HolographicProgressBar(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    color: Color = SystemNeonBlue,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(12.dp)) {
        val width = size.width
        val height = size.height
        val cornerRadius = CornerRadius(height / 2, height / 2)

        // Background track
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = cornerRadius
        )

        // Outer glow/border track
        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            size = size,
            cornerRadius = cornerRadius,
            style = Stroke(width = 1.dp.toPx())
        )

        // Progress bar
        if (animatedProgress > 0f) {
            val progressWidth = width * animatedProgress

            // Draw main progress
            drawRoundRect(
                color = color,
                size = Size(width = progressWidth, height = height),
                cornerRadius = cornerRadius
            )

            // Add futuristic segmented details to the progress bar
            val segmentWidth = 10.dp.toPx()
            val spacing = 2.dp.toPx()
            val totalSegments = (progressWidth / (segmentWidth + spacing)).toInt()

            for (i in 0 until totalSegments) {
                val startX = i * (segmentWidth + spacing)
                if (startX + segmentWidth < progressWidth) {
                     drawLine(
                        color = Color.Black.copy(alpha = 0.4f),
                        start = Offset(startX + segmentWidth, 0f),
                        end = Offset(startX + segmentWidth + spacing, height),
                        strokeWidth = spacing
                    )
                }
            }

            // Add a bright highlight at the end of the progress bar
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(progressWidth - 4.dp.toPx(), 0f),
                size = Size(width = 4.dp.toPx(), height = height),
                cornerRadius = cornerRadius
            )
        }
    }
}
