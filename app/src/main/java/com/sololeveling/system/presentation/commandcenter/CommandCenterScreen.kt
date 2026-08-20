package com.sololeveling.system.presentation.commandcenter

import androidx.compose.foundation.background
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.presentation.components.SystemPanel

@Composable
fun CommandCenterScreen(
    viewModel: CommandCenterViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit
) {
    val player by viewModel.playerState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(viewModel.healthConnectManager.requiredPermissions)) {
            viewModel.syncHealthData()
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CommandCenterViewModel.UiEvent.RequestHealthPermissions -> {
                    permissionLauncher.launch(event.permissions)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            player?.let { p ->
                PlayerSummaryPanel(p, onClick = onNavigateToProfile)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SystemPanel(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "DAILY QUESTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeQuests.isEmpty()) {
                        Text(
                            text = "All quests completed for today.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        activeQuests.forEach { quest ->
                            QuestItem(quest = quest)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SystemPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.syncHealthData() }
            ) {
                Text(
                    text = "SYNC SYSTEM DATA (HEALTH CONNECT)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun QuestItem(quest: Quest) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "[${quest.type.name}] ${quest.title}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "+${quest.xpReward} XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = quest.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        quest.requiredActivity?.let { req ->
            val progress = (req.currentValue / req.targetValue).toFloat().coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${req.currentValue.toInt()} / ${req.targetValue.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PlayerSummaryPanel(player: Player, onClick: () -> Unit = {}) {
    SystemPanel(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column {
            Text(
                text = "STATUS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LEVEL ${player.level}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "RANK ${player.rank.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "XP: ${player.xp} / ${player.nextLevelXp}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
