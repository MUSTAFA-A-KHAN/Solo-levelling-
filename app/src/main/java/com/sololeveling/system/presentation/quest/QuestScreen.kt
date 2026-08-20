package com.sololeveling.system.presentation.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.presentation.components.SystemPanel

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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { showCompleted = false }) {
                    Text(
                        "ACTIVE (${activeQuests.size})",
                        color = if (!showCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { showCompleted = true }) {
                    Text(
                        "COMPLETED (${completedQuests.size})",
                        color = if (showCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val displayList = if (showCompleted) completedQuests else activeQuests

            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
fun QuestItem(quest: Quest, onAddProgress: (Double) -> Unit) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${quest.type.name}] ${quest.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Rank: ${quest.difficulty.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            quest.requiredActivity?.let { req ->
                val progressPercent = if (req.targetValue > 0) {
                    (req.currentValue / req.targetValue).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Progress: ${req.currentValue.toInt()} / ${req.targetValue.toInt()} ${req.activityType.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progressPercent,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (!quest.isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAddProgress(1.0) }, // Default increment for demo/manual addition
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add Progress")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rewards: ${quest.xpReward} XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            if (quest.attributeRewards.isNotEmpty()) {
                val attrs = quest.attributeRewards.entries.joinToString(", ") { "${it.value} ${it.key.name}" }
                Text(
                    text = "+ $attrs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
