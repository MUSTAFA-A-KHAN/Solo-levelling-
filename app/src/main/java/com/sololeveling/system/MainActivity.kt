package com.sololeveling.system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sololeveling.system.presentation.awakening.AwakeningScreen
import com.sololeveling.system.presentation.commandcenter.CommandCenterScreen
import com.sololeveling.system.presentation.commandcenter.CommandCenterViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.sololeveling.system.presentation.profile.ProfileScreen
import com.sololeveling.system.presentation.quest.QuestScreen
import com.sololeveling.system.presentation.theme.SystemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val viewModel: CommandCenterViewModel = hiltViewModel()
                    val playerState by viewModel.playerState.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDestination = if (playerState == null) "awakening" else "command_center"

                        NavHost(navController = navController, startDestination = startDestination) {
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
