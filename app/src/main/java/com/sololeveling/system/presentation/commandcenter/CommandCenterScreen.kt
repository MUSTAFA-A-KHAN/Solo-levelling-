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
import com.sololeveling.system.presentation.components.SystemPanel

@Composable
fun CommandCenterScreen(
    viewModel: CommandCenterViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToQuests: () -> Unit
) {
    val player by viewModel.playerState.collectAsState()

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

            SystemPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToQuests() }
            ) {
                Column {
                    Text(
                        text = "QUEST LOG",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "View active and completed quests.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
