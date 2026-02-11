package dev.belalkhan.cutthenoise.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ReframeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CutTheNoiseDatabase : RoomDatabase() {
    abstract fun reframeDao(): ReframeDao
}
