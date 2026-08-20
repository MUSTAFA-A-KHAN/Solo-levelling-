package com.sololeveling.system.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sololeveling.system.data.local.dao.PlayerDao
import com.sololeveling.system.data.local.entity.PlayerEntity

@Database(entities = [PlayerEntity::class], version = 1, exportSchema = false)
abstract class SystemDatabase : RoomDatabase() {
    abstract val playerDao: PlayerDao
}
