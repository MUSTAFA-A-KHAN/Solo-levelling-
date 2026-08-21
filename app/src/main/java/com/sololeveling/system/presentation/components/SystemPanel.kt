package com.sololeveling.system.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sololeveling.system.presentation.theme.SystemNeonBlue
import android.os.Build

@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SystemNeonBlue,
    borderWidth: Dp = 1.dp,
    cutoutSize: Dp = 16.dp,
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

    // Custom shape with chamfered corners (sci-fi cutout)
    val shape = object : Shape {
        override fun createOutline(
            size: androidx.compose.ui.geometry.Size,
            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
            density: androidx.compose.ui.unit.Density
        ): Outline {
            val cutPx = with(density) { cutoutSize.toPx() }
            val path = Path().apply {
                moveTo(0f, cutPx)
                lineTo(cutPx, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height - cutPx)
                lineTo(size.width - cutPx, size.height)
                lineTo(0f, size.height)
                close()
            }
            return Outline.Generic(path)
        }
    }

    val panelColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)

    // Add glass blur effect for newer Android versions, fallback to opacity for older
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(radius = 16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .drawBehind {
                val glowRadius = 16.dp.toPx()
                val strokeWidthPx = borderWidth.toPx()
                val cutPx = cutoutSize.toPx()

                val path = Path().apply {
                    moveTo(0f, cutPx)
                    lineTo(cutPx, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - cutPx)
                    lineTo(size.width - cutPx, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                // Outer Glow
                val paint = Paint().asFrameworkPaint().apply {
                    color = borderColor.copy(alpha = alphaAnim).toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokeWidthPx
                    maskFilter = android.graphics.BlurMaskFilter(glowRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                }

                drawIntoCanvas { canvas ->
                    canvas.drawPath(
                        path = path,
                        paint = androidx.compose.ui.graphics.Paint().apply {
                            asFrameworkPaint().set(paint)
                        }
                    )
                }
            }
            .clip(shape)
            .then(blurModifier)
            .background(panelColor)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                        borderColor.copy(alpha = 0.05f)
                    )
                )
            )
            .drawBehind {
                val cutPx = cutoutSize.toPx()
                val path = Path().apply {
                    moveTo(0f, cutPx)
                    lineTo(cutPx, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - cutPx)
                    lineTo(size.width - cutPx, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                // Crisp inner border
                drawPath(
                    path = path,
                    color = borderColor.copy(alpha = 0.7f),
                    style = Stroke(width = borderWidth.toPx())
                )
            }
            .padding(16.dp)
    ) {
        content()
    }
}
