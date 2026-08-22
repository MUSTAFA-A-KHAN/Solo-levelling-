package com.sololeveling.system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sololeveling.system.data.local.SystemPreferences
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.presentation.awakening.AwakeningScreen
import com.sololeveling.system.presentation.commandcenter.CommandCenterScreen
import com.sololeveling.system.presentation.commandcenter.CommandCenterViewModel
import com.sololeveling.system.presentation.profile.ProfileScreen
import com.sololeveling.system.presentation.quest.QuestScreen
import com.sololeveling.system.presentation.theme.SystemTheme
import com.sololeveling.system.presentation.welcome.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.firstOrNull

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var systemPreferences: SystemPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val viewModel: CommandCenterViewModel = hiltViewModel()
                    val isLoading by viewModel.isLoading.collectAsState()

                    var startDestination by remember { mutableStateOf<String?>(null) }

                    androidx.compose.runtime.LaunchedEffect(isLoading) {
                        if (!isLoading) {
                            val hasFirebaseUser = authRepository.authState.firstOrNull() != null
                            val welcomeWasShown = systemPreferences.welcomeShown.firstOrNull() ?: false
                            val hasPlayer = viewModel.playerState.value != null
                            startDestination = when {
                                !hasFirebaseUser && !welcomeWasShown && !hasPlayer -> "welcome"
                                !hasPlayer -> "awakening"
                                else -> "command_center"
                            }

                            if (hasFirebaseUser) {
                                authRepository.syncAccountOnLaunch()
                            }
                        }
                    }

                    if (isLoading || startDestination == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavHost(navController = navController, startDestination = startDestination!!) {
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToAwakening = {
                                    navController.navigate("awakening") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onNavigateToCommandCenter = {
                                    navController.navigate("command_center") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("awakening") {
                            AwakeningScreen(
                                onAwakeningComplete = {
                                    viewModel.completeAwakening()
                                    navController.navigate("command_center") {
                                        popUpTo("awakening") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("command_center") {
                            CommandCenterScreen(
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToQuests = { navController.navigate("quests") }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("quests") {
                            QuestScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}
