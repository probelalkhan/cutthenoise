package dev.belalkhan.cutthenoise.llm

interface LocalLlmEngine {
    suspend fun infer(prompt: String): String
}
