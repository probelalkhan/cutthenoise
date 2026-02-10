package dev.belalkhan.cutthenoise.data.local.llm

interface LlmCallback {
    fun onToken(token: String)
}