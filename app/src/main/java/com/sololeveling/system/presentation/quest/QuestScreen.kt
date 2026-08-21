package com.sololeveling.system.presentation.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.HolographicProgressBar
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuestViewModel = hiltViewModel()
) {
    val activeQuests by viewModel.activeQuests.collectAsState()
    val completedQuests by viewModel.completedQuests.collectAsState()

    var showCompleted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QUEST LOG", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AtmosphericBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Futuristic segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!showCompleted) SystemNeonBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f))
                            .clickable { showCompleted = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ACTIVE (${activeQuests.size})",
                            color = if (!showCompleted) SystemNeonBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (showCompleted) SystemNeonPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f))
                            .clickable { showCompleted = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "COMPLETED (${completedQuests.size})",
                            color = if (showCompleted) SystemNeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val displayList = if (showCompleted) completedQuests else activeQuests

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("NO QUESTS AVAILABLE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(displayList) { quest ->
                            QuestItem(
                                quest = quest,
                                onAddProgress = { amount -> viewModel.addProgress(quest.id, amount) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestItem(quest: Quest, onAddProgress: (Double) -> Unit) {
    val borderColor = if (quest.isCompleted) SystemNeonPurple else SystemNeonBlue

    val rankColor = when (quest.difficulty.name) {
        "S" -> RankSColor
        "A" -> RankAColor
        "B" -> RankBColor
        "C" -> RankCColor
        "D" -> RankDColor
        else -> RankEColor
    }

    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MISSION: ${quest.type.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor
                    )
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .background(rankColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RANK ${quest.difficulty.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = rankColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress
            quest.requiredActivity?.let { req ->
                val progressPercent = if (req.targetValue > 0) {
                    (req.currentValue / req.targetValue).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "OBJECTIVE PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${req.currentValue.toInt()} / ${req.targetValue.toInt()} ${req.activityType.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                HolographicProgressBar(
                    progress = progressPercent,
                    color = borderColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = borderColor,
                    modifier = Modifier.align(Alignment.End)
                )

                if (!quest.isCompleted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAddProgress(100.0) },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Add Progress")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Rewards
            Text(
                text = "REWARDS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "+ ${quest.xpReward} XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = SystemNeonBlueVariant
                )
                if (quest.attributeRewards.isNotEmpty()) {
                    val attrs = quest.attributeRewards.entries.joinToString(", ") { "+${it.value} ${it.key.name.take(3)}" }
                    Text(
                        text = attrs,
                        style = MaterialTheme.typography.titleMedium,
                        color = SystemNeonPurple
                    )
                }
            }
        }
    }
}
