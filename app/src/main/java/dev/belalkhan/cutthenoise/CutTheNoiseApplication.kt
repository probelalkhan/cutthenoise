package dev.belalkhan.cutthenoise

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.belalkhan.cutthenoise.data.local.llm.LlmInferenceSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class CutTheNoiseApplication : Application() {

    @Inject
    lateinit var llmInferenceSource: LlmInferenceSource

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            llmInferenceSource.loadModel()
        }
    }
}
