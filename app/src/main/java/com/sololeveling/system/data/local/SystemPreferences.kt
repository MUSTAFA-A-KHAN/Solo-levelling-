package com.sololeveling.system.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "system_preferences")

@Singleton
class SystemPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LAST_DAILY_QUEST_DATE = stringPreferencesKey("last_daily_quest_date")
        private val LAST_WEEKLY_QUEST_DATE = stringPreferencesKey("last_weekly_quest_date")
        private val WELCOME_SHOWN = booleanPreferencesKey("welcome_shown")
    }

    val lastDailyQuestDate: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_DAILY_QUEST_DATE]
        }

    val lastWeeklyQuestDate: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_WEEKLY_QUEST_DATE]
        }

    val welcomeShown: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WELCOME_SHOWN] ?: false
        }

    suspend fun setLastDailyQuestDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_DAILY_QUEST_DATE] = date
        }
    }

    suspend fun setLastWeeklyQuestDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_WEEKLY_QUEST_DATE] = date
        }
    }

    suspend fun setWelcomeShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WELCOME_SHOWN] = shown
        }
    }
}
