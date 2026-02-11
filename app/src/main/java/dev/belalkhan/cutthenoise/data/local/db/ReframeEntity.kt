package dev.belalkhan.cutthenoise.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reframes")
data class ReframeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val thought: String,
    val stoicResponse: String,
    val strategistResponse: String,
    val optimistResponse: String,
    val createdAt: Long = System.currentTimeMillis()
)
