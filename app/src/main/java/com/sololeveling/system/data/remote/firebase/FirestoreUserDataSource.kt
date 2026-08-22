package com.sololeveling.system.data.remote.firebase

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserDataSource @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun upsertAccount(user: FirebaseUser) {
        val docRef = firestore
            .collection(USERS_COLLECTION)
            .document(user.uid)
            .collection(ACCOUNT_SUBCOLLECTION)
            .document(ACCOUNT_DOC_ID)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val now = System.currentTimeMillis()

            if (snapshot.exists()) {
                transaction.update(
                    docRef,
                    mapOf(
                        FIELD_DISPLAY_NAME to (user.displayName ?: ""),
                        FIELD_EMAIL to (user.email ?: ""),
                        FIELD_PHOTO_URL to (user.photoUrl?.toString() ?: ""),
                        FIELD_LAST_LOGIN_AT to now
                    )
                )
            } else {
                val account = AccountDocument(
                    uid = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    createdAt = now,
                    lastLoginAt = now
                )
                transaction.set(docRef, account)
            }
        }.await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ACCOUNT_SUBCOLLECTION = "account"
        private const val ACCOUNT_DOC_ID = "current"
        private const val FIELD_DISPLAY_NAME = "displayName"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_PHOTO_URL = "photoUrl"
        private const val FIELD_LAST_LOGIN_AT = "lastLoginAt"
    }
}
