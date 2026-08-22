package com.sololeveling.system.data.remote.firebase

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserDataSource @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun upsertAccount(user: FirebaseUser) {
        val now = System.currentTimeMillis()
        val accountId = user.uid
        val displayName = user.displayName ?: ""
        val email = user.email ?: ""
        val photoUrl = user.photoUrl?.toString() ?: ""

        var createdAt: Long? = null

        val snapshot = kotlin.runCatching {
            firestore
                .collection(USERS_COLLECTION)
                .document(accountId)
                .collection(ACCOUNT_SUBCOLLECTION)
                .document(ACCOUNT_DOC_ID)
                .get()
                .await()
        }

        if (snapshot.isSuccess && snapshot.getOrNull()?.exists() == true) {
            createdAt = snapshot.getOrNull()!!.getLong(FIELD_CREATED_AT) ?: now
        } else {
            createdAt = createdAt ?: now
        }

        val account = AccountDocument(
            uid = accountId,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            createdAt = createdAt,
            lastLoginAt = now
        )

        retryOnConnectivityError {
            firestore
                .collection(USERS_COLLECTION)
                .document(accountId)
                .collection(ACCOUNT_SUBCOLLECTION)
                .document(ACCOUNT_DOC_ID)
                .set(account)
                .await()
        }
    }

    private suspend fun <T> retryOnConnectivityError(
        maxRetries: Int = 3,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        for (attempt in 0..maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (isConnectivityRelated(e) && attempt < maxRetries) {
                    val delayMs = (1L shl attempt) * 1000L
                    kotlinx.coroutines.delay(delayMs)
                } else {
                    throw e
                }
            }
        }
        throw lastException!!
    }

    private fun isConnectivityRelated(e: Exception): Boolean {
        return e is FirebaseFirestoreException &&
            e.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
            e is FirebaseNetworkException ||
            e is UnknownHostException ||
            e.cause is UnknownHostException
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ACCOUNT_SUBCOLLECTION = "account"
        private const val ACCOUNT_DOC_ID = "current"
        private const val FIELD_DISPLAY_NAME = "displayName"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_PHOTO_URL = "photoUrl"
        private const val FIELD_LAST_LOGIN_AT = "lastLoginAt"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
