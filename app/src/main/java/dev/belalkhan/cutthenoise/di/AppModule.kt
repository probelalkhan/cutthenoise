package dev.belalkhan.cutthenoise.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.belalkhan.cutthenoise.data.local.llm.LlmInferenceSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLlmInferenceSource(
        @ApplicationContext context: Context
    ): LlmInferenceSource {
        return LlmInferenceSource(context)
    }
}
