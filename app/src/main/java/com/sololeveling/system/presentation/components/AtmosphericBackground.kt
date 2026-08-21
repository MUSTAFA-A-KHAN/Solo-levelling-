package com.sololeveling.system.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.sololeveling.system.presentation.theme.SystemBackground
import com.sololeveling.system.presentation.theme.SystemNeonBlue
import com.sololeveling.system.presentation.theme.SystemNeonPurpleVariant
import com.sololeveling.system.presentation.theme.SystemSurfaceVariant
import kotlin.random.Random

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_y"
    )

    // Generate random particles once
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                alpha = Random.nextFloat() * 0.5f + 0.1f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SystemBackground,
                        SystemSurfaceVariant.copy(alpha = 0.8f),
                        SystemBackground
                    )
                )
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SystemNeonPurpleVariant.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    radius = 1500f
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw a subtle technological grid
            val gridSize = 100f
            val gridColor = SystemNeonBlue.copy(alpha = 0.03f)

            for (x in 0..width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(width, y.toFloat()),
                    strokeWidth = 1f
                )
            }

            // Draw animated particles floating upwards
            particles.forEach { particle ->
                // Calculate position with animation wrapping around the screen height
                val currentY = (height + (particle.yOffset * height) - (offsetY * particle.speed)) % height
                // Ensure currentY is positive due to modulo behavior with negative numbers (if speed was high enough, not here though)
                val finalY = if (currentY < 0) currentY + height else currentY

                drawCircle(
                    color = SystemNeonBlue.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = Offset(particle.x * width, finalY)
                )
            }
        }

        content()
    }
}

private data class Particle(
    val x: Float,
    val yOffset: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)
