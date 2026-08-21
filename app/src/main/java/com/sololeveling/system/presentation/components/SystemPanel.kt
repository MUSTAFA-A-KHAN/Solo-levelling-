package com.sololeveling.system.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sololeveling.system.presentation.theme.SystemNeonBlue

@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SystemNeonBlue,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    // Subtle pulsing animation for the glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val shape = RoundedCornerShape(4.dp)
    val panelColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val gradientColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.Transparent
    )

    Box(
        modifier = modifier
            .drawBehind {
                val glowRadius = 16.dp.toPx()
                val strokeWidthPx = borderWidth.toPx()
                val paint = Paint().asFrameworkPaint().apply {
                    color = borderColor.copy(alpha = alphaAnim).toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokeWidthPx
                    maskFilter = android.graphics.BlurMaskFilter(glowRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 4.dp.toPx(),
                        radiusY = 4.dp.toPx(),
                        paint = androidx.compose.ui.graphics.Paint().apply {
                            asFrameworkPaint().set(paint)
                        }
                    )
                }
            }
            .clip(shape)
            .background(panelColor)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .border(
                width = borderWidth,
                color = borderColor.copy(alpha = 0.6f),
                shape = shape
            )
            .padding(16.dp)
    ) {
        content()
    }
}
