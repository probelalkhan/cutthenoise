package dev.belalkhan.cutthenoise.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReframeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ReframeEntity): Long

    @Query("SELECT * FROM reframes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReframeEntity>>

    @Query("SELECT * FROM reframes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<ReframeEntity>>

    @Query("SELECT * FROM reframes WHERE id = :id")
    fun getById(id: Long): ReframeEntity?

    @Query("SELECT * FROM reframes WHERE thought LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<ReframeEntity>>
}
