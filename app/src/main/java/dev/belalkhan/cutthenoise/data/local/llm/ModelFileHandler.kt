package dev.belalkhan.cutthenoise.data.local.llm

import android.content.Context
import java.io.File

object ModelFileHandler {

    private const val MODEL_NAME =
        "tinyllama-1.1b-chat-v1.0.Q5_K_M.gguf"

    fun prepare(context: Context): File {
        val outFile = File(context.filesDir, MODEL_NAME)
        if (outFile.exists()) return outFile

        context.assets.open(MODEL_NAME).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }
}