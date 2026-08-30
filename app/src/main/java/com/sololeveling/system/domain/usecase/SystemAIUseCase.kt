package com.sololeveling.system.domain.usecase

import com.sololeveling.system.data.notifications.SystemNotificationManager
import com.sololeveling.system.domain.model.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemAIUseCase @Inject constructor(
    private val notificationManager: SystemNotificationManager
) {
    fun processCommand(command: String, player: Player?): String {
        val cmd = command.lowercase()
        return when {
            cmd.contains("drink") || cmd.contains("water") -> {
                "System: Hydration recorded. Vitality parameters stabilizing."
            }
            cmd.contains("status") -> {
                "System: Player ${player?.name ?: "Unknown"} at Level ${player?.level ?: 0}. Rank ${player?.rank?.name ?: "N/A"}."
            }
            else -> {
                "System: Command recognized. Analyzing player growth potential..."
            }
        }
    }

    fun triggerDailyEncouragement(player: Player) {
        val message = if (player.level < 10) {
            "System: Even the weakest hunters must start somewhere. Continue your training."
        } else {
            "System: Your growth is exceeding expectations. The path to the Shadow Monarch awaits."
        }
        notificationManager.showNotification("SYSTEM MESSAGE", message)
    }
}
