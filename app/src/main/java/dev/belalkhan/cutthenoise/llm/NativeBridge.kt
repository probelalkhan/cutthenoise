package dev.belalkhan.cutthenoise.llm

object NativeBridge {
    init {
        System.loadLibrary("llm")
    }

    external fun initModel(modelPath: String)

    // Updated signature
    external fun runInference(prompt: String, callback: LlmCallback): String
}

interface LlmCallback {
    fun onToken(token: String)
}