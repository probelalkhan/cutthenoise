package dev.belalkhan.cutthenoise.data.local.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlmInferenceSource(context: Context) {

    init {
        val modelFile = ModelFileHandler.prepare(context)
        NativeLlmBridge.initModel(modelFile.absolutePath)
    }

    suspend fun infer(prompt: String, onToken: (String) -> Unit): String =
        withContext(Dispatchers.Default) {
            NativeLlmBridge.runInference(prompt, object : LlmCallback {
                override fun onToken(token: String) {
                    onToken(token)
                }
            })
        }
}