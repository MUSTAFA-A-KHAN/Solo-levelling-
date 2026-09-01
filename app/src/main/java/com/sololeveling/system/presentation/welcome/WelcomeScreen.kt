package com.sololeveling.system.presentation.welcome

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.system.R
import com.sololeveling.system.presentation.components.AtmosphericBackground
import com.sololeveling.system.presentation.components.SystemPanel
import com.sololeveling.system.presentation.theme.SystemNeonBlue
import com.sololeveling.system.presentation.theme.SystemNeonPurple
import com.sololeveling.system.presentation.theme.StatusError
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onNavigateToAwakening: () -> Unit,
    onNavigateToCommandCenter: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            viewModel.onSignInResult(result.data)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is WelcomeViewModel.WelcomeEvent.LaunchSignIn -> {
                    signInLauncher.launch(event.intent)
                }
                is WelcomeViewModel.WelcomeEvent.SignInSuccess -> {
                    onNavigateToCommandCenter()
                }
                is WelcomeViewModel.WelcomeEvent.NavigateToAwakening -> {
                    onNavigateToAwakening()
                }
                is WelcomeViewModel.WelcomeEvent.NavigateToCommandCenter -> {
                    onNavigateToCommandCenter()
                }
            }
        }
    }

    AtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arise_logo),
                contentDescription = "Solo Leveling Arise",
                modifier = Modifier
                    .height(200.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "SYSTEM INITIALIZATION",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "WELCOME, NEW PLAYER",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            WelcomeActionButton(
                title = "SIGN IN WITH GOOGLE",
                subtitle = "Sync progress across devices",
                borderColor = SystemNeonBlue,
                isLoading = isLoading,
                onClick = { viewModel.onSignInWithGoogle() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            WelcomeActionButton(
                title = "CONTINUE WITHOUT SIGNING IN",
                subtitle = "Play locally, sync later",
                borderColor = SystemNeonPurple,
                isLoading = false,
                enabled = !isLoading,
                onClick = { viewModel.onContinueWithoutSignIn() }
            )

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusError,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeActionButton(
    title: String,
    subtitle: String,
    borderColor: androidx.compose.ui.graphics.Color,
    isLoading: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (isLoading) {
        SystemPanel(
            modifier = Modifier.fillMaxWidth(),
            borderColor = borderColor.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = borderColor,
                    strokeWidth = 2.dp
                )
            }
        }
    } else {
        SystemPanel(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClickLabel = title, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
            borderColor = borderColor
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = borderColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
