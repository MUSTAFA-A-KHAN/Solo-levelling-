package com.sololeveling.system.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sololeveling.system.data.local.dao.PlayerDao
import com.sololeveling.system.data.local.dao.QuestDao
import com.sololeveling.system.data.local.entity.PlayerEntity
import com.sololeveling.system.data.local.entity.QuestEntity

@Database(entities = [PlayerEntity::class, QuestEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SystemDatabase : RoomDatabase() {
    abstract val playerDao: PlayerDao
    abstract val questDao: QuestDao
}
