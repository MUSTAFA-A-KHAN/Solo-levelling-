package com.sololeveling.system.presentation.commandcenter

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.components.PlayerStatusCard
import com.sololeveling.system.presentation.theme.*

@Preview(
    showBackground = true,
    backgroundColor = 0xFF05070D,
    widthDp = 390,
    heightDp = 2000
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
            Text("SYSTEM ONLINE", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
            Text("COMMAND CENTER", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 4.sp)
            Spacer(Modifier.height(24.dp))
            PlayerStatusCard(player = Player(name = "Sung Jin-Woo", title = "The Shadow Monarch", rank = Rank.S, level = 87, xp = 7420, nextLevelXp = 10000))
            Spacer(Modifier.height(24.dp))
            HealthOverviewPanel(DailyHealthData(steps = 8421, workoutMinutes = 47, caloriesBurned = 426.0, sleepMinutes = 452), weeklySteps = 52340, totalSteps = 184230)
            Spacer(Modifier.height(24.dp))
            HydrationPanel(
                current = 1.2, 
                goal = 2.0, 
                reminderEnabled = true,
                reminderInterval = 60,
                onAddWater = {},
                onToggleReminder = {},
                onSetInterval = {},
                onReset = {}
            )
            Spacer(Modifier.height(24.dp))
            AITerminalPanel(response = "System: All parameters normal. Awaiting further growth.", onSendCommand = {})
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
    val aiResponse by viewModel.aiResponse.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(viewModel.healthConnectManager.requiredPermissions)) {
            viewModel.syncHealthData()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleHydrationReminders(true)
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CommandCenterViewModel.UiEvent.RequestHealthPermissions -> {
                    permissionLauncher.launch(event.permissions)
                }
                CommandCenterViewModel.UiEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    AtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            val scrollState = rememberScrollState()
            val headerCollapsed by derivedStateOf { scrollState.value > 8 }

            AnimatedVisibility(
                visible = !headerCollapsed,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "SYSTEM ONLINE", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                    ConnectionStatusIndicator(status = connectionStatus, onDismissError = { viewModel.clearConnectionError() })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "COMMAND CENTER", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 4.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            player?.let { p -> PlayerStatusCard(p, onClick = onNavigateToProfile) }
            Spacer(modifier = Modifier.height(16.dp))
            DashboardActions(activeQuests.size, onNavigateToQuests, onNavigateToLeaderboard, onSync = { viewModel.syncHealthData() })
            Spacer(modifier = Modifier.height(16.dp))

            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("STATUS", "SYSTEM", "ARMY")
            LaunchedEffect(selectedTab) { scrollState.scrollTo(0) }
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                when (selectedTab) {
                    0 -> {
                        HealthOverviewPanel(dailyHealthData, weeklySteps, player?.footsteps ?: 0L)
                        Spacer(modifier = Modifier.height(24.dp))
                        player?.let { p ->
                            HydrationPanel(
                                current = p.hydrationData.currentIntakeLiters,
                                goal = p.hydrationData.dailyGoalLiters,
                                reminderEnabled = p.hydrationData.reminderEnabled,
                                reminderInterval = p.hydrationData.reminderIntervalMinutes,
                                onAddWater = { viewModel.addHydration(it) },
                                onToggleReminder = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleHydrationReminders(enabled)
                                    }
                                },
                                onSetInterval = { viewModel.setHydrationReminderInterval(it) },
                                onReset = { viewModel.resetHydration() }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    1 -> {
                        // Test Notification Button
                        SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonPurple) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "SYSTEM DIAGNOSTICS",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SystemNeonPurple,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.sendTestNotification() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SystemNeonPurple.copy(alpha = 0.2f),
                                        contentColor = SystemNeonPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "SEND TEST NOTIFICATION",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to verify notification system is operational",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        AITerminalPanel(response = aiResponse, onSendCommand = { viewModel.sendAICommand(it) })
                        Spacer(modifier = Modifier.height(24.dp))
                        DailyQuestOverview(activeQuests)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    2 -> {
                        ShadowArmyPanel()
                        Spacer(modifier = Modifier.height(24.dp))
                        QuotesPanel()
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (syncUiState is CommandCenterViewModel.PlayerSyncUiState.Conflict) {
        SyncConflictDialog(
            remoteLevel = (syncUiState as CommandCenterViewModel.PlayerSyncUiState.Conflict).remote.level,
            onConfirm = { viewModel.confirmRemoteOverride() },
            onDismiss = { viewModel.dismissConflict() }
        )
    }
}

@Composable
fun DashboardActions(activeCount: Int, onQuests: () -> Unit, onLeaderboard: () -> Unit, onSync: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SystemPanel(modifier = Modifier.weight(1f).clickable { onQuests() }) {
            ActionItem("QUEST LOG", "$activeCount ACTIVE", MaterialTheme.colorScheme.primary)
        }
        SystemPanel(modifier = Modifier.weight(1f).clickable { onLeaderboard() }, borderColor = SystemNeonPurple) {
            ActionItem("LEADERBOARD", "RANKINGS", SystemNeonPurple)
        }
        SystemPanel(modifier = Modifier.weight(1f).clickable { onSync() }, borderColor = MaterialTheme.colorScheme.secondary) {
            ActionItem("SYNC DATA", "HEALTH", MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ActionItem(title: String, subtitle: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HydrationPanel(
    current: Double,
    goal: Double,
    reminderEnabled: Boolean,
    reminderInterval: Int,
    onAddWater: (Double) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSetInterval: (Int) -> Unit,
    onReset: () -> Unit
) {
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonBlue) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "RECOVERY STATUS: HYDRATION", style = MaterialTheme.typography.labelLarge, color = SystemNeonBlue)
                IconButton(onClick = { onToggleReminder(!reminderEnabled) }) {
                    Icon(
                        imageVector = if (reminderEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = "Toggle Reminders",
                        tint = if (reminderEnabled) SystemNeonBlue else Color.Gray
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                 Text(text = "${String.format("%.1f", current)} / ${String.format("%.1f", goal)} L", style = MaterialTheme.typography.titleMedium, color = Color.White)
                 if (reminderEnabled) {
                     Text(text = "System alerts every ${reminderInterval}m", style = MaterialTheme.typography.labelSmall, color = SystemNeonBlue.copy(alpha = 0.7f))
                 }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HolographicProgressBar(progress = (current / goal).toFloat().coerceIn(0f, 1f), color = SystemNeonBlue)
            
            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "ADJUST ALERT FREQUENCY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90, 120).forEach { mins ->
                        HydrationButton(
                            label = "${mins}m",
                            onClick = { onSetInterval(mins) },
                            modifier = Modifier.weight(1f),
                            isSelected = reminderInterval == mins
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HydrationButton(label = "+250ml", onClick = { onAddWater(0.25) }, modifier = Modifier.weight(1f))
                HydrationButton(label = "+500ml", onClick = { onAddWater(0.5) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HydrationButton(
                label = "RESET",
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                isSelected = false
            )
        }
    }
}

@Composable
fun HydrationButton(
    label: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val containerColor = if (isSelected) SystemNeonBlue.copy(alpha = 0.3f) else SystemNeonBlue.copy(alpha = 0.1f)
    val borderColor = if (isSelected) SystemNeonBlue else SystemNeonBlue.copy(alpha = 0.5f)
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = SystemNeonBlue),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AITerminalPanel(response: String, onSendCommand: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = Color.White.copy(alpha = 0.5f)) {
        Column {
            Text(text = "SYSTEM TERMINAL", style = MaterialTheme.typography.labelLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(text = response, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    placeholder = { Text("Enter command...", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
                    textStyle = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = { 
                    onSendCommand(text)
                    text = ""
                }) {
                    Text("EXECUTE", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(status: CommandCenterViewModel.ConnectionStatus, onDismissError: () -> Unit) {
    val (dotColor, dotPulse, errorMessage, shouldShowError) = when (status) {
        is CommandCenterViewModel.ConnectionStatus.Connected -> Quad(StatusSuccess, false, null, false)
        is CommandCenterViewModel.ConnectionStatus.Syncing -> Quad(StatusWarning, true, null, false)
        is CommandCenterViewModel.ConnectionStatus.Failed -> Quad(StatusError, true, status.message, true)
        is CommandCenterViewModel.ConnectionStatus.Idle -> Quad(MaterialTheme.colorScheme.onSurfaceVariant, false, null, false)
    }

    val animatedDotColor by animateColorAsState(targetValue = dotColor, animationSpec = tween(durationMillis = if (dotPulse) 600 else 300))

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(modifier = Modifier.size(if (dotPulse) 12.dp else 10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(animatedDotColor))
        Spacer(modifier = Modifier.width(8.dp))
        if (shouldShowError && errorMessage != null) {
            Text(text = "SYNC ERROR: $errorMessage", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = StatusError, modifier = Modifier.clickable { onDismissError() })
        } else {
            Text(text = "CLOUD CONNECTED", style = MaterialTheme.typography.labelMedium, color = if (dotPulse) StatusWarning else StatusSuccess)
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SyncConflictDialog(remoteLevel: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("EXISTING PROGRESS FOUND", color = MaterialTheme.colorScheme.primary) },
        text = { Text("Your Google account already has saved progress (Level $remoteLevel). Restoring it will replace your local progress on this device. Continue?", color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("RESTORE CLOUD", color = MaterialTheme.colorScheme.primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("KEEP LOCAL", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun HealthOverviewPanel(data: DailyHealthData, weeklySteps: Long = 0L, totalSteps: Long = 0L) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(text = "TODAY's OVERVIEW", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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
fun HealthStatMetric(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
fun DailyQuestOverview(quests: List<Quest>) {
    val dailyQuests = quests.filter { it.type == com.sololeveling.system.domain.model.QuestType.DAILY }
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = if (dailyQuests.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary) {
        Column {
            Text(text = "DAILY QUESTS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))
            if (dailyQuests.isEmpty()) {
                Text(text = "No active daily quests. Wait for system reset.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                dailyQuests.forEach { quest ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = quest.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        quest.requiredActivity?.let { req ->
                            val progress = if (req.targetValue > 0) (req.currentValue / req.targetValue).toFloat().coerceIn(0f, 1f) else 0f
                            Box(modifier = Modifier.width(100.dp)) {
                                HolographicProgressBar(progress = progress, color = if (progress >= 1f) StatusSuccess else MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuotesPanel() {
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonPurple) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Quotes", style = MaterialTheme.typography.labelLarge, color = SystemNeonPurple, letterSpacing = 2.sp)
            QuoteImage(modifier = Modifier, drawable = R.drawable.img_elixir_glow, label = "“The system will not give you what you want. It will give you what you earn.”")
        }
    }
}

@Composable
fun ShadowArmyPanel() {
    SystemPanel(modifier = Modifier.fillMaxWidth(), borderColor = SystemNeonPurple) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "SHADOW ARMY", style = MaterialTheme.typography.labelLarge, color = SystemNeonPurple, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CharacterPortrait(modifier = Modifier.weight(1f), drawable = R.drawable.img_cha_hae_in, label = "CHA HAE-IN")
                CharacterPortrait(modifier = Modifier.weight(1f), drawable = R.drawable.img_igris, label = "IGRIS", imageOffsetX = (-19).dp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CharacterPortrait(modifier = Modifier.weight(1f), drawable = R.drawable.img_baro, label = "BARO")
            }
        }
    }
}

@Composable
fun CharacterPortrait(modifier: Modifier = Modifier, drawable: Int, label: String, imageOffsetX: Dp = 0.dp) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))) {
            AnimatedFireEffect(modifier = Modifier.fillMaxSize())
            Image(painter = painterResource(id = drawable), contentDescription = label, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().offset(x = imageOffsetX))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun QuoteImage(modifier: Modifier = Modifier, drawable: Int, label: String, imageOffsetX: Dp = 0.dp) {
    Box(modifier = modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))) {
        AnimatedFireEffect(modifier = Modifier.fillMaxSize())
        Image(painter = painterResource(id = drawable), contentDescription = label, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().offset(x = imageOffsetX).alpha(0.5f))
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp))
    }
}
