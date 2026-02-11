package dev.belalkhan.cutthenoise.data.repository

import dev.belalkhan.cutthenoise.data.local.db.ReframeDao
import dev.belalkhan.cutthenoise.data.local.db.ReframeEntity
import dev.belalkhan.cutthenoise.domain.repository.ReframeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReframeRepositoryImpl @Inject constructor(
    private val dao: ReframeDao
) : ReframeRepository {

    override suspend fun saveReframe(
        thought: String,
        stoicResponse: String,
        strategistResponse: String,
        optimistResponse: String
    ): Long {
        val entity = ReframeEntity(
            thought = thought,
            stoicResponse = stoicResponse,
            strategistResponse = strategistResponse,
            optimistResponse = optimistResponse
        )
        return withContext(Dispatchers.IO) {
            dao.insert(entity)
        }
    }

    override fun getAllReframes(): Flow<List<ReframeEntity>> {
        return dao.getAll()
    }

    override fun getRecentReframes(limit: Int): Flow<List<ReframeEntity>> {
        return dao.getRecent(limit)
    }

    override suspend fun getReframeById(id: Long): ReframeEntity? {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    override fun searchReframes(query: String): Flow<List<ReframeEntity>> {
        return dao.search(query)
    }
}
