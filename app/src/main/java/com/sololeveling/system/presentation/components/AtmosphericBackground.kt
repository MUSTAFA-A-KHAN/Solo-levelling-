package com.sololeveling.system.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.sololeveling.system.presentation.theme.SystemBackground
import com.sololeveling.system.presentation.theme.SystemSurfaceVariant
import com.sololeveling.system.presentation.theme.SystemNeonPurpleVariant

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SystemBackground,
                        SystemSurfaceVariant.copy(alpha = 0.5f),
                        SystemBackground
                    )
                )
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SystemNeonPurpleVariant.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    radius = 1200f
                )
            )
    ) {
        content()
    }
}
