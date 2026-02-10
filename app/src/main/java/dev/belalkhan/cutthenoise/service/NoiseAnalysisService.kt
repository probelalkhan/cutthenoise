package dev.belalkhan.cutthenoise.service

import dev.belalkhan.cutthenoise.domain.NoiseAnalysisResult

interface NoiseAnalysisService {
    suspend fun analyze(text: String): NoiseAnalysisResult
}
