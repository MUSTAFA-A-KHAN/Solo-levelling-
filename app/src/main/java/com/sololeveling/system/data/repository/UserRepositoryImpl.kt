package com.sololeveling.system.data.repository

import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.data.remote.firebase.FirestoreUserDataSource
import com.sololeveling.system.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestoreUserDataSource: FirestoreUserDataSource
) : UserRepository {

    override suspend fun createOrUpdateAccount(user: FirebaseUser) {
        firestoreUserDataSource.upsertAccount(user)
    }
}
