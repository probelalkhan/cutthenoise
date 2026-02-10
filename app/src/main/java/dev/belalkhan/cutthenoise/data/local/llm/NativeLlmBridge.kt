package dev.belalkhan.cutthenoise.data.local.llm

object NativeLlmBridge {
    init {
        System.loadLibrary("llm")
    }

    external fun initModel(modelPath: String)
    external fun runInference(prompt: String, callback: LlmCallback): String
}