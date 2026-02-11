package dev.belalkhan.cutthenoise.data.local.llm

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LlmInferenceSource(private val context: Context) {

    private val isLoaded = AtomicBoolean(false)
    private val loadMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun loadModel() {
        if (isLoaded.get()) return
        
        loadMutex.withLock {
            if (isLoaded.get()) return
            withContext(Dispatchers.IO) {
                val modelFile = ModelFileHandler.prepare(context)
                NativeLlmBridge.initModel(modelFile.absolutePath)
                isLoaded.set(true)
            }
        }
    }

    suspend fun infer(prompt: String, onToken: (String) -> Unit) {
        if (!isLoaded.get()) {
            loadModel()
        }
        
        withContext(Dispatchers.Default) {
            NativeLlmBridge.runInference(prompt, object : LlmCallback {
                override fun onToken(token: String) {
                    onToken(token)
                }
            })
        }
    }
}