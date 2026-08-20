package com.sololeveling.system.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerAttributes
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.components.AtmosphericBackground

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val player by viewModel.playerState.collectAsState()

    AtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "PLAYER PROFILE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            player?.let { p ->
                IdentityPanel(p)
                Spacer(modifier = Modifier.height(16.dp))
                AttributesPanel(p.attributes, p.availableAttributePoints)
            }
        }
    }
}

@Composable
fun IdentityPanel(player: Player) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "NAME: ${player.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "LEVEL: ${player.level}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RANK: ${player.rank.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "XP: ${player.xp} / ${player.nextLevelXp}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AttributesPanel(attributes: PlayerAttributes, availablePoints: Int) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.secondary
    ) {
        Column {
            Text(
                text = "ATTRIBUTES",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            AttributeRow("STRENGTH (STR)", attributes.strength)
            AttributeRow("VITALITY (VIT)", attributes.vitality)
            AttributeRow("AGILITY (AGI)", attributes.agility)
            AttributeRow("INTELLIGENCE (INT)", attributes.intelligence)
            AttributeRow("DISCIPLINE (DIS)", attributes.discipline)
            AttributeRow("ENDURANCE (END)", attributes.endurance)

            if (availablePoints > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "UNALLOCATED POINTS: $availablePoints",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AttributeRow(name: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
