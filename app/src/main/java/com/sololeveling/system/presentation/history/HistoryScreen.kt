package com.sololeveling.system.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.HolographicProgressBar
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val summaries by viewModel.summaries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QUEST HISTORY", color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp) },
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
            if (summaries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "NO HISTORY YET",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(summaries) { summary ->
                        DailySummaryCard(summary = summary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySummaryCard(summary: DailySummary) {
    val dateLabel = if (summary.date.isBlank()) "Unknown date" else summary.date
    val completionRatio = if (summary.total > 0) summary.completed.toFloat() / summary.total.toFloat() else 0f

    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${summary.completed}/${summary.total} DONE",
                    style = MaterialTheme.typography.labelMedium,
                    color = SystemNeonBlue
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+ ${summary.xpEarned} XP earned",
                style = MaterialTheme.typography.labelMedium,
                color = SystemNeonBlueVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            HolographicProgressBar(progress = completionRatio, color = SystemNeonPurple)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            summary.quests.forEach { quest ->
                HistoryQuestRow(quest = quest)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryQuestRow(quest: Quest) {
    val rankColor = when (quest.difficulty.name) {
        "S" -> RankSColor
        "A" -> RankAColor
        "B" -> RankBColor
        "C" -> RankCColor
        "D" -> RankDColor
        else -> RankEColor
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (quest.isCompleted) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (quest.isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = quest.type.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "RANK ${quest.difficulty.name}",
            style = MaterialTheme.typography.labelSmall,
            color = rankColor
        )
    }
}
