package com.sololeveling.system.data.remote.firebase

data class AccountDocument(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L
)
