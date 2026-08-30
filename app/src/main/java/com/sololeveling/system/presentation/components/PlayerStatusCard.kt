package com.sololeveling.system.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.system.R
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Rank
import com.sololeveling.system.presentation.theme.*

@Composable
fun PlayerStatusCard(
    player: Player,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val rankColor = when (player.rank) {
        Rank.S -> RankSColor
        Rank.A -> RankAColor
        Rank.B -> RankBColor
        Rank.C -> RankCColor
        Rank.D -> RankDColor
        Rank.E -> RankEColor
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(402f / 127f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF030914),
                        Color(0xFF050B1A),
                        Color(0xFF0B0A24)
                    )
                )
            )
            .clickable(
                onClickLabel = "View Player Profile",
                role = androidx.compose.ui.semantics.Role.Button
            ) { onClick() }
            .drawBehind {

                // Outer cyan HUD border
                drawRoundRect(
                    color = Color(0xFF00BFFF).copy(alpha = 0.75f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx()
                    )
                )

                // Inner purple glow
                drawRoundRect(
                    color = Color(0xFF315CFF).copy(alpha = 0.22f),
                    cornerRadius = CornerRadius(9.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx()
                    )
                )

                // Top-left HUD corner
                val p1 = Path().apply {
                    moveTo(0f, 17.dp.toPx())
                    lineTo(0f, 6.dp.toPx())
                    lineTo(11.dp.toPx(), 0f)
                }

                drawPath(
                    path = p1,
                    color = Color(0xFF00C8FF),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Bottom-left HUD corner
                val p2 = Path().apply {
                    moveTo(0f, size.height - 17.dp.toPx())
                    lineTo(0f, size.height - 6.dp.toPx())
                    lineTo(11.dp.toPx(), size.height)
                }

                drawPath(
                    path = p2,
                    color = Color(0xFF00C8FF),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Top-right HUD corner
                val p3 = Path().apply {
                    moveTo(size.width, 17.dp.toPx())
                    lineTo(size.width, 6.dp.toPx())
                    lineTo(size.width - 11.dp.toPx(), 0f)
                }

                drawPath(
                    path = p3,
                    color = Color(0xFF00C8FF),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Bottom-right HUD corner
                val p4 = Path().apply {
                    moveTo(size.width, size.height - 17.dp.toPx())
                    lineTo(size.width, size.height - 6.dp.toPx())
                    lineTo(size.width - 11.dp.toPx(), size.height)
                }

                drawPath(
                    path = p4,
                    color = Color(0xFF00C8FF),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
    ) {

        // Purple/blue glow behind character
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.52f)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF24206D).copy(alpha = 0.35f),
                            Color(0xFF3526A0).copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Character artwork
        Image(
            painter = painterResource(R.drawable.img_jinwoo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.53f)
                .align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 14.dp,
                    top = 7.dp,
                    bottom = 7.dp,
                    end = 12.dp
                )
        ) {

            // PLAYER STATUS
            Text(
                text = "PLAYER STATUS",
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = Color(0xFF00D9FF)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // RANK HEXAGON
                RankEmblem(
                    rank = player.rank,
                    size = 58.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 2.dp)
                ) {

                    Text(
                        text = player.name.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.7.sp,
                        color = Color(0xFFE8ECF7),
                        maxLines = 1
                    )

                    Text(
                        text = (
                                player.title
                                    ?: "HE WHO LEVELS ALONE"
                                ).uppercase(),
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.7.sp,
                        color = Color(0xFF777EA8),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "LEVEL ${player.level}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF00D9FF)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        HolographicProgressBar(
                            progress = if (player.nextLevelXp > 0) {
                                (
                                        player.xp.toFloat() /
                                                player.nextLevelXp.toFloat()
                                        ).coerceIn(0f, 1f)
                            } else {
                                0f
                            },
                            color = Color(0xFF00D9FF),
                            backgroundColor = Color(0xFF14223B),
                            modifier = Modifier
                                .width(122.dp)
                                .height(8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${player.xp} / ${player.nextLevelXp} XP",
                            fontSize = 6.sp,
                            letterSpacing = 0.7.sp,
                            color = Color(0xFF858CA8),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Small HUD decoration on top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 7.dp, end = 42.dp)
                .width(8.dp)
                .height(2.dp)
                .background(
                    Color(0xFF0876FF)
                )
        )

        // Small HUD decoration bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 5.dp)
                .width(24.dp)
                .height(1.dp)
                .background(
                    Color(0xFF00BFFF).copy(alpha = 0.65f)
                )
        )
    }
}