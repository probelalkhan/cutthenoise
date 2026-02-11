package dev.belalkhan.cutthenoise.domain.repository

import dev.belalkhan.cutthenoise.data.local.db.ReframeEntity
import kotlinx.coroutines.flow.Flow

interface ReframeRepository {
    suspend fun saveReframe(
        thought: String,
        stoicResponse: String,
        strategistResponse: String,
        optimistResponse: String
    ): Long

    fun getAllReframes(): Flow<List<ReframeEntity>>

    fun getRecentReframes(limit: Int): Flow<List<ReframeEntity>>
}
