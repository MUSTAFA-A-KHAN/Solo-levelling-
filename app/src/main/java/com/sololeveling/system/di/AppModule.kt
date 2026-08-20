package com.sololeveling.system.di

import android.content.Context
import androidx.room.Room
import com.sololeveling.system.data.local.SystemDatabase
import com.sololeveling.system.data.local.dao.PlayerDao
import com.sololeveling.system.data.repository.PlayerRepositoryImpl
import com.sololeveling.system.domain.repository.PlayerRepository
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
    fun providePlayerRepository(playerDao: PlayerDao): PlayerRepository {
        return PlayerRepositoryImpl(playerDao)
    }

    @Provides
    @Singleton
    fun provideProgressionEngine(): ProgressionEngine {
        return ProgressionEngine()
    }
}
