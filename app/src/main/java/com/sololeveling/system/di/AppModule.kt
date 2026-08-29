package com.sololeveling.system.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).fallbackToDestructiveMigration().build()
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE quest_table ADD COLUMN date TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE player_table ADD COLUMN footsteps INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE player_table ADD COLUMN hydrationData TEXT NOT NULL DEFAULT " +
                    "'{\"dailyGoalLiters\":2.0,\"currentIntakeLiters\":0.0,\"lastDrinkTimestamp\":0,\"logs\":[]}'"
            )
        }
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
