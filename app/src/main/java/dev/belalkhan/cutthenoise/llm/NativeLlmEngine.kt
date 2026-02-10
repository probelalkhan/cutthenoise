package dev.belalkhan.cutthenoise.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NativeLlmEngine(context: Context) {

    init {
        val modelFile = ModelLoader.prepare(context)
        NativeBridge.initModel(modelFile.absolutePath)
    }

    suspend fun infer(prompt: String, onToken: (String) -> Unit): String =
        withContext(Dispatchers.Default) {
            NativeBridge.runInference(prompt, object : LlmCallback {
                override fun onToken(token: String) {
                    onToken(token)
                }
            })
        }
}
