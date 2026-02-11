package dev.belalkhan.cutthenoise

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.belalkhan.cutthenoise.data.local.llm.LlmInferenceSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@HiltAndroidApp
class CutTheNoiseApplication : Application() {

    @javax.inject.Inject
    lateinit var llmInferenceSource: LlmInferenceSource

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            llmInferenceSource.loadModel()
        }
    }
}
