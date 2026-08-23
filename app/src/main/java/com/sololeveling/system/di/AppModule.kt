package com.sololeveling.system.di

import android.content.Context
import androidx.room.Room
import com.sololeveling.system.data.local.SystemDatabase
import com.sololeveling.system.data.local.dao.PlayerDao
import com.sololeveling.system.data.local.dao.QuestDao
import com.sololeveling.system.data.remote.auth.AuthRepositoryImpl
import com.sololeveling.system.data.remote.firebase.FirestoreLeaderboardDataSource
import com.sololeveling.system.data.remote.firebase.FirestorePlayerDataSource
import com.sololeveling.system.data.remote.firebase.FirestoreQuestDataSource
import com.sololeveling.system.data.remote.firebase.FirestoreUserDataSource
import com.sololeveling.system.data.repository.LeaderboardRepositoryImpl
import com.sololeveling.system.data.repository.PlayerRepositoryImpl
import com.sololeveling.system.data.repository.QuestRepositoryImpl
import com.sololeveling.system.data.repository.UserRepositoryImpl
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import com.sololeveling.system.domain.repository.QuestRepository
import com.sololeveling.system.domain.repository.UserRepository
import com.sololeveling.system.domain.usecase.ProgressionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSystemDatabase(@ApplicationContext context: Context): SystemDatabase {
        return Room.databaseBuilder(
            context,
            SystemDatabase::class.java,
            "system_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providePlayerDao(db: SystemDatabase): PlayerDao = db.playerDao

    @Provides
    @Singleton
    fun providePlayerRepository(
        playerDao: PlayerDao,
        firestorePlayerDataSource: FirestorePlayerDataSource,
        authRepository: AuthRepository
    ): PlayerRepository {
        return PlayerRepositoryImpl(playerDao, firestorePlayerDataSource, authRepository)
    }

    @Provides
    @Singleton
    fun provideQuestDao(db: SystemDatabase): QuestDao = db.questDao

    @Provides
    @Singleton
    fun provideQuestRepository(
        questDao: QuestDao,
        firestoreQuestDataSource: FirestoreQuestDataSource,
        authRepository: AuthRepository
    ): QuestRepository {
        return QuestRepositoryImpl(questDao, firestoreQuestDataSource, authRepository)
    }

    @Provides
    @Singleton
    fun provideProgressionEngine(): ProgressionEngine {
        return ProgressionEngine()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context, userRepository: UserRepository): AuthRepository {
        return AuthRepositoryImpl(context, userRepository)
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestoreUserDataSource: FirestoreUserDataSource): UserRepository {
        return UserRepositoryImpl(firestoreUserDataSource)
    }

    @Provides
    @Singleton
    fun provideLeaderboardRepository(
        firestoreLeaderboardDataSource: FirestoreLeaderboardDataSource,
        authRepository: AuthRepository
    ): LeaderboardRepository {
        return LeaderboardRepositoryImpl(firestoreLeaderboardDataSource, authRepository)
    }
}
