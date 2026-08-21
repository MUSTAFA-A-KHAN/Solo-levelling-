package com.sololeveling.system.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sololeveling.system.presentation.theme.SystemNeonBlue
import com.sololeveling.system.presentation.theme.SystemNeonPurple
import android.os.Build

@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SystemNeonBlue,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    // Soft, low-intensity glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // 18-22dp rounded corners per requirements
    val cornerRadius = 20.dp
    val shape = RoundedCornerShape(cornerRadius)

    // Authentic glassmorphism: 10-20% alpha for frosted finish
    val panelColor = Color.White.copy(alpha = 0.15f)

    val blurEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurEffect(radiusX = 20f, radiusY = 20f, edgeTreatment = TileMode.Clamp)
    } else null

    Box(modifier = modifier) {
        // Background layer with blur and glow effects
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val glowRadius = 12.dp.toPx()
                    val strokeWidthPx = borderWidth.toPx()
                    val cornerPx = cornerRadius.toPx()

                    // Soft Outer Glow
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
                            radiusX = cornerPx,
                            radiusY = cornerPx,
                            paint = androidx.compose.ui.graphics.Paint().apply {
                                asFrameworkPaint().set(paint)
                            }
                        )
                    }
                }
                .clip(shape)
                .then(
                    if (blurEffect != null) {
                        Modifier.graphicsLayer {
                            renderEffect = blurEffect
                        }
                    } else Modifier
                )
                .background(panelColor)
                // Subtle cyan/purple glass tint
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SystemNeonBlue.copy(alpha = 0.05f),
                            SystemNeonPurple.copy(alpha = 0.05f)
                        )
                    )
                )
                .drawBehind {
                    val cornerPx = cornerRadius.toPx()
                    // 1dp gradient border, simulating light catching the edge of glass
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        size = size,
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = borderWidth.toPx())
                    )
                }
        )

        // Content layer rendered sharply on top
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
