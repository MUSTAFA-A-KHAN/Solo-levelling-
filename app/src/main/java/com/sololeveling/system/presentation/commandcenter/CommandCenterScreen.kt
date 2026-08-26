package com.sololeveling.system.presentation.commandcenter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.R
import com.sololeveling.system.domain.model.Rank
import com.sololeveling.system.presentation.components.AnimatedFireEffect
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.HolographicProgressBar
import com.sololeveling.system.presentation.components.RankEmblem
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.components.PlayerStatusCard
import com.sololeveling.system.presentation.theme.*

@Preview(
    showBackground = true,
    backgroundColor = 0xFF05070D,
    widthDp = 390,
    heightDp = 1600
)
@Composable
fun CommandCenterScreenPreview() {
    SystemTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(32.dp))

            Text(
                "SYSTEM ONLINE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "COMMAND CENTER",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(24.dp))

            PlayerStatusCard(
                player = Player(
                    name = "Sung Jin-Woo",
                    title = "The Shadow Monarch",
                    rank = Rank.S,
                    level = 87,
                    xp = 7420,
                    nextLevelXp = 10000
                )
            )

            Spacer(Modifier.height(24.dp))

            HealthOverviewPanel(
                DailyHealthData(
                    steps = 8421,
                    workoutMinutes = 47,
                    caloriesBurned = 426.00,
                    sleepMinutes = 452
                ),
                weeklySteps = 52340,
                totalSteps = 184230
            )

            Spacer(Modifier.height(24.dp))

            ShadowArmyPanel()

            Spacer(Modifier.height(24.dp))

            QuotesPanel()

            Spacer(Modifier.height(32.dp))
        }
    }
}
@Composable
fun CommandCenterScreen(
    viewModel: CommandCenterViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToQuests: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val player by viewModel.playerState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val dailyHealthData by viewModel.dailyHealthData.collectAsState()
    val weeklySteps by viewModel.weeklySteps.collectAsState()
    val syncUiState by viewModel.syncUiState.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

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

    AtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SYSTEM ONLINE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ConnectionStatusIndicator(
                status = connectionStatus,
                onDismissError = { viewModel.clearConnectionError() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

             player?.let { p ->

                PlayerStatusCard(p, onClick = onNavigateToProfile)

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SystemPanel(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToQuests() }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "QUEST LOG",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeQuests.size} ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SystemPanel(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLeaderboard() },
                    borderColor = SystemNeonPurple
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LEADERBOARD",
                            style = MaterialTheme.typography.titleMedium,
                            color = SystemNeonPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RANKINGS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SystemPanel(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.syncHealthData() },
                    borderColor = MaterialTheme.colorScheme.secondary
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SYNC DATA",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HEALTH CONNECT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HealthOverviewPanel(dailyHealthData, weeklySteps, player?.footsteps ?: 0L)

            Spacer(modifier = Modifier.height(24.dp))

            DailyQuestOverview(activeQuests)

            Spacer(modifier = Modifier.height(24.dp))

            ShadowArmyPanel()

            Spacer(modifier = Modifier.height(32.dp))

            QuotesPanel()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (syncUiState is CommandCenterViewModel.PlayerSyncUiState.Conflict) {
        val remote = (syncUiState as CommandCenterViewModel.PlayerSyncUiState.Conflict).remote
        AlertDialog(
            onDismissRequest = { viewModel.dismissConflict() },
            title = { Text("EXISTING PROGRESS FOUND", color = MaterialTheme.colorScheme.primary) },
            text = {
                Text(
                    "Your Google account already has saved progress (Level ${remote.level}). " +
                        "Restoring it will replace your local progress on this device. " +
                        "Continue?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRemoteOverride() }) {
                    Text("RESTORE CLOUD", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConflict() }) {
                    Text("KEEP LOCAL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun ConnectionStatusIndicator(
    status: CommandCenterViewModel.ConnectionStatus,
    onDismissError: () -> Unit
) {
    val dotColor: Color
    val dotPulse: Boolean
    val errorMessage: String?
    val shouldShowError: Boolean

    when (status) {
        is CommandCenterViewModel.ConnectionStatus.Connected -> {
            dotColor = StatusSuccess
            dotPulse = false
            errorMessage = null
            shouldShowError = false
        }
        is CommandCenterViewModel.ConnectionStatus.Syncing -> {
            dotColor = StatusWarning
            dotPulse = true
            errorMessage = null
            shouldShowError = false
        }
        is CommandCenterViewModel.ConnectionStatus.Failed -> {
            dotColor = StatusError
            dotPulse = true
            errorMessage = status.message
            shouldShowError = true
        }
        is CommandCenterViewModel.ConnectionStatus.Idle -> {
            dotColor = MaterialTheme.colorScheme.onSurfaceVariant
            dotPulse = false
            errorMessage = null
            shouldShowError = false
        }
    }

    val animatedDotColor by animateColorAsState(
        targetValue = dotColor,
        animationSpec = tween(durationMillis = if (dotPulse) 600 else 300)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (dotPulse) 12.dp else 10.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(animatedDotColor)
        )

        if (shouldShowError && errorMessage != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYNC ERROR: $errorMessage",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusError,
                modifier = Modifier.clickable { onDismissError() }
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CLOUD CONNECTED",
                style = MaterialTheme.typography.labelMedium,
                color = if (dotPulse) StatusWarning else StatusSuccess
            )
        }
    }
}

@Composable
fun PlayerSummaryPanel(player: Player, onClick: () -> Unit = {}) {
    SystemPanel(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankEmblem(rank = player.rank, size = 72.dp)

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                player.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "LEVEL ${player.level}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${player.xp} / ${player.nextLevelXp} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val progress = if (player.nextLevelXp > 0) (player.xp.toFloat() / player.nextLevelXp.toFloat()) else 0f
                HolographicProgressBar(progress = progress)
            }
        }
    }
}

@Composable
fun HealthOverviewPanel(data: DailyHealthData, weeklySteps: Long = 0L, totalSteps: Long = 0L) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "TODAY's OVERVIEW",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthStatMetric(label = "STEPS", value = data.steps.toString(), color = SystemNeonBlue)
                HealthStatMetric(label = "ACTIVE", value = "${data.workoutMinutes}m", color = StatusSuccess)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthStatMetric(label = "CALORIES", value = "${data.caloriesBurned.toInt()} kcal", color = StatusWarning)
                HealthStatMetric(label = "SLEEP", value = "${data.sleepMinutes / 60}h ${data.sleepMinutes % 60}m", color = SystemNeonPurple)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthStatMetric(label = "WEEKLY STEPS", value = weeklySteps.toString(), color = StatusSuccess)
                HealthStatMetric(label = "TOTAL STEPS", value = totalSteps.toString(), color = SystemNeonBlue)
            }
        }
    }
}

@Composable
fun HealthStatMetric(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun DailyQuestOverview(quests: List<Quest>) {
    val dailyQuests = quests.filter { it.type == com.sololeveling.system.domain.model.QuestType.DAILY }

    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (dailyQuests.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary
    ) {
        Column {
            Text(
                text = "DAILY QUESTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (dailyQuests.isEmpty()) {
                Text(
                    text = "No active daily quests. Wait for system reset.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                dailyQuests.forEach { quest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quest.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        quest.requiredActivity?.let { req ->
                            val progress = if (req.targetValue > 0) (req.currentValue / req.targetValue).toFloat().coerceIn(0f, 1f) else 0f
                            Box(modifier = Modifier.width(100.dp)) {
                                HolographicProgressBar(
                                    progress = progress,
                                    color = if (progress >= 1f) StatusSuccess else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuotesPanel(){
     SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonPurple) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quotes",
                style = MaterialTheme.typography.labelLarge,
                color = SystemNeonPurple,
                letterSpacing = 2.sp
            )
            QuoteImage(
                modifier = Modifier,
                drawable = R.drawable.img_elixir_glow,
                label = "“The system will not give you what you want. It will give you what you earn.”"
            )
        }
    }
}

@Composable
fun ShadowArmyPanel() {
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonPurple) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SHADOW ARMY",
                style = MaterialTheme.typography.labelLarge,
                color = SystemNeonPurple,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CharacterPortrait(
                    modifier = Modifier.weight(1f),
                    drawable = R.drawable.img_cha_hae_in,
                    label = "CHA HAE-IN"
                )
                CharacterPortrait(
                    modifier = Modifier.weight(1f),
                    drawable = R.drawable.img_igris,
                    label = "IGRIS",
                    imageOffsetX = (-19).dp
                )
            }

             Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CharacterPortrait(
                    modifier = Modifier.weight(1f),
                    drawable = R.drawable.img_baro,
                    label = "BARO"
                )
            }
        }

    }
}

@Composable
fun CharacterPortrait(
    modifier: Modifier = Modifier,
    drawable: Int,
    label: String,
    imageOffsetX: Dp = 0.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AnimatedFireEffect(
                modifier = Modifier.fillMaxSize()
            )

            Image(
                painter = painterResource(id = drawable),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = imageOffsetX)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuoteImage(
    modifier: Modifier = Modifier,
    drawable: Int,
    label: String,
    imageOffsetX: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AnimatedFireEffect(
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = drawable),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = imageOffsetX)
                .alpha(0.5f)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }
}