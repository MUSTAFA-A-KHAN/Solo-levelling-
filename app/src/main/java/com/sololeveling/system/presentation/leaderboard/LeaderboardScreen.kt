package com.sololeveling.system.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.domain.model.LeaderboardEntry
import com.sololeveling.system.domain.model.Rank
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.RankEmblem
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.theme.RankAColor
import com.sololeveling.system.presentation.theme.RankSColor
import com.sololeveling.system.presentation.theme.StatusSuccess
import com.sololeveling.system.presentation.theme.StatusWarning
import com.sololeveling.system.presentation.theme.SystemNeonBlue
import com.sololeveling.system.presentation.theme.SystemNeonPurple
import com.sololeveling.system.presentation.theme.SystemNeonPurpleVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUid = viewModel.authRepository.getCurrentUser()?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HUNTER LEADERBOARD", color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(onClick = onNavigateToProfile) {
                            Text(
                                text = "PROFILE",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
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
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    !isAuthenticated -> {
                        SystemPanel(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = SystemNeonPurple
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ACCOUNT NOT LINKED",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SystemNeonPurple,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Link your account to view the global leaderboard and compete with other hunters.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SystemNeonBlue.copy(alpha = 0.15f))
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClickLabel = "Link Account", role = androidx.compose.ui.semantics.Role.Button) { onNavigateToProfile() }
                                        .padding(vertical = 10.dp, horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "LINK ACCOUNT",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SystemNeonBlue
                                    )
                                }
                            }
                        }
                    }
                    errorMessage != null -> {
                        SystemPanel(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = StatusWarning
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "CLOUD SIGNAL LOST",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StatusWarning,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SCANNING THE DATABASE...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    entries.isEmpty() -> {
                        SystemPanel(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "NO HUNTERS FOUND",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Be the first to link your account and appear on the leaderboard.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "RANKED HUNTERS (${entries.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(entries, key = { it.uid }) { entry ->
                                val position = entries.indexOfFirst { it.uid == entry.uid } + 1
                                LeaderboardItem(
                                    entry = entry,
                                    rank = position,
                                    isSelf = entry.uid == currentUid,
                                    onClick = if (entry.uid == currentUid) onNavigateToProfile else null
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
fun LeaderboardItem(
    entry: LeaderboardEntry,
    rank: Int,
    isSelf: Boolean,
    onClick: (() -> Unit)?
) {
    val rankColor = when (entry.rank) {
        Rank.S -> RankSColor
        Rank.A -> RankAColor
        else -> when (rank) {
            1 -> RankSColor
            2 -> SystemNeonBlue
            3 -> StatusWarning
            else -> SystemNeonPurple
        }
    }

    val podiumColor = when (rank) {
        1 -> RankSColor
        2 -> RankAColor
        3 -> SystemNeonPurple
        else -> SystemNeonPurpleVariant
    }

    val panelBorder = if (isSelf) SystemNeonBlue else podiumColor

    val itemModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClickLabel = "View Profile", role = androidx.compose.ui.semantics.Role.Button, onClick = { onClick() }) else Modifier)
        .clip(RoundedCornerShape(20.dp))

    SystemPanel(
        modifier = itemModifier,
        borderColor = panelBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(podiumColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Text(
                        text = podiumEmoji(rank),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = podiumColor
                    )
                } else {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            RankEmblem(rank = entry.rank, size = 48.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.displayName.takeIf { it.isNotBlank() } ?: "HUNTER ${entry.uid.take(6)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelf) SystemNeonBlue else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelf) {
                            Text(
                                text = "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                color = SystemNeonBlue
                            )
                        }
                    }

                    Text(
                        text = "LV. ${entry.level}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = rankColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${entry.xp} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "QUESTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${entry.completedQuests}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StatusSuccess
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "STEPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = entry.footsteps.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SystemNeonPurpleVariant
                )
            }

            if (!entry.photoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun podiumEmoji(rank: Int): String = when (rank) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> ""
}
