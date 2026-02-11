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

    suspend fun getReframeById(id: Long): ReframeEntity?

    fun getAllReframes(): Flow<List<ReframeEntity>>

    fun getRecentReframes(limit: Int): Flow<List<ReframeEntity>>

    fun searchReframes(query: String): Flow<List<ReframeEntity>>
}
