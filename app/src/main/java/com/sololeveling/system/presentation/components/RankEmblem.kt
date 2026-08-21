package com.sololeveling.system.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.system.domain.model.Rank
import com.sololeveling.system.presentation.theme.*

@Composable
fun RankEmblem(
    rank: Rank,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val rankColor = when (rank) {
        Rank.S -> RankSColor
        Rank.A -> RankAColor
        Rank.B -> RankBColor
        Rank.C -> RankCColor
        Rank.D -> RankDColor
        Rank.E -> RankEColor
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.toPx()
            val height = size.toPx()

            // Draw a futuristic hexagon
            val path = Path().apply {
                moveTo(width / 2f, 0f)
                lineTo(width, height * 0.25f)
                lineTo(width, height * 0.75f)
                lineTo(width / 2f, height)
                lineTo(0f, height * 0.75f)
                lineTo(0f, height * 0.25f)
                close()
            }

            // Fill with subtle gradient/opacity
            drawPath(
                path = path,
                color = rankColor.copy(alpha = 0.2f)
            )

            // Outer crisp border
            drawPath(
                path = path,
                color = rankColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner accent lines for a tech feel
            val innerPath = Path().apply {
                val padding = width * 0.15f
                moveTo(width / 2f, padding)
                lineTo(width - padding, height * 0.25f + padding * 0.5f)
                lineTo(width - padding, height * 0.75f - padding * 0.5f)
                lineTo(width / 2f, height - padding)
                lineTo(padding, height * 0.75f - padding * 0.5f)
                lineTo(padding, height * 0.25f + padding * 0.5f)
                close()
            }

            drawPath(
                path = innerPath,
                color = rankColor.copy(alpha = 0.5f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Text(
            text = rank.name,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.5f).sp
            ),
            color = rankColor
        )
    }
}
