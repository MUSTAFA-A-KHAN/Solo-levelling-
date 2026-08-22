package com.sololeveling.system.domain.model

sealed class PlayerSyncResult {
    object NoAction : PlayerSyncResult()
    object UploadedLocal : PlayerSyncResult()
    object DownloadedRemote : PlayerSyncResult()
    data class Conflict(val remote: Player, val local: Player) : PlayerSyncResult()
}
