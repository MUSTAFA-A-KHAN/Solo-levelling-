package com.sololeveling.system.presentation.profile

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerAttributes
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.HolographicProgressBar
import com.sololeveling.system.presentation.components.RankEmblem
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val player by viewModel.playerState.collectAsState()
    val authUser by viewModel.authUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            viewModel.handleSignInResult(result.data)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PLAYER STATUS", color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp) },
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
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AccountSection(
                    authUser = authUser,
                    onSignIn = { signInLauncher.launch(viewModel.getSignInIntent()) },
                    onSignOut = { viewModel.signOut() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                player?.let { p ->
                    IdentityPanel(p)
                    Spacer(modifier = Modifier.height(24.dp))
                    AttributesPanel(p.attributes, p.availableAttributePoints)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AccountSection(
    authUser: FirebaseUser?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (authUser != null) SystemNeonBlue else SystemNeonPurple
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (authUser != null) "LINKED ACCOUNT" else "ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (authUser != null) {
                Text(
                    text = authUser.displayName ?: "Hunter",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = authUser.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusError.copy(alpha = 0.15f))
                        .clickable { onSignOut() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIGN OUT",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusError
                    )
                }
            } else {
                Text(
                    text = "Not linked. Sign in to sync progress across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SystemNeonBlue.copy(alpha = 0.15f))
                        .clickable { onSignIn() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIGN IN WITH GOOGLE",
                        style = MaterialTheme.typography.titleMedium,
                        color = SystemNeonBlue
                    )
                }
            }
        }
    }
}

@Composable
fun IdentityPanel(player: Player) {
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RankEmblem(rank = player.rank, size = 80.dp)

                Spacer(modifier = Modifier.width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = player.name.uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    player.title?.let { title ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TITLE: ${title.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SystemNeonPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "LEVEL ${player.level}",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 42.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "XP ${player.xp} / ${player.nextLevelXp}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (player.nextLevelXp > 0) (player.xp.toFloat() / player.nextLevelXp.toFloat()) else 0f
            HolographicProgressBar(progress = progress, color = SystemNeonBlue)
        }
    }
}

@Composable
fun AttributesPanel(attributes: PlayerAttributes, availablePoints: Int) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = SystemNeonPurple
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATTRIBUTES",
                    style = MaterialTheme.typography.titleLarge,
                    color = SystemNeonPurple,
                    letterSpacing = 2.sp
                )

                if (availablePoints > 0) {
                    Box(modifier = Modifier.background(StatusWarning.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "POINTS: $availablePoints",
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // In Solo Leveling early on, 100 is a good baseline visual max for attributes before they go wild.
            val maxAttr = 100.0

            AttributeRow("STRENGTH", "STR", attributes.strength, maxAttr)
            AttributeRow("VITALITY", "VIT", attributes.vitality, maxAttr)
            AttributeRow("AGILITY", "AGI", attributes.agility, maxAttr)
            AttributeRow("INTELLIGENCE", "INT", attributes.intelligence, maxAttr)
            AttributeRow("DISCIPLINE", "DIS", attributes.discipline, maxAttr)
            AttributeRow("ENDURANCE", "END", attributes.endurance, maxAttr)
        }
    }
}

@Composable
fun AttributeRow(name: String, abbr: String, value: Double, maxVisualValue: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = abbr,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SystemNeonPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progress = (value / maxVisualValue).toFloat().coerceIn(0f, 1f)
        HolographicProgressBar(
            progress = progress,
            color = SystemNeonPurple,
            backgroundColor = SystemNeonPurple.copy(alpha = 0.1f)
        )
    }
}
