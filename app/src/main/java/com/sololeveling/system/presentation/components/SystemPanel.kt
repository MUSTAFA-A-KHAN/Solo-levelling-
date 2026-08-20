package com.sololeveling.system.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sololeveling.system.presentation.theme.SystemNeonBlue

@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SystemNeonBlue.copy(alpha = 0.5f),
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, spotColor = borderColor, ambientColor = borderColor)
            .background(MaterialTheme.colorScheme.surface, shape = RectangleShape)
            .border(width = borderWidth, color = borderColor, shape = RectangleShape)
            .padding(16.dp)
    ) {
        content()
    }
}
