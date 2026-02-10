package dev.belalkhan.cutthenoise.llm

import android.content.Context
import java.io.File

fun copyModelIfNeeded(context: Context): File {
    val outFile = File(context.filesDir, "tinyllama-1.1b-chat-v1.0.Q5_K_M.gguf")
    if (outFile.exists()) return outFile

    context.assets.open("tinyllama-1.1b-chat-v1.0.Q5_K_M.gguf").use { input ->
        outFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return outFile
}
