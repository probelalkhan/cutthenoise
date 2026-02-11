package dev.belalkhan.cutthenoise.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.belalkhan.cutthenoise.data.repository.LlmRepositoryImpl
import dev.belalkhan.cutthenoise.data.repository.ReframeRepositoryImpl
import dev.belalkhan.cutthenoise.domain.repository.LlmRepository
import dev.belalkhan.cutthenoise.domain.repository.ReframeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLlmRepository(
        llmRepositoryImpl: LlmRepositoryImpl
    ): LlmRepository

    @Binds
    @Singleton
    abstract fun bindReframeRepository(
        reframeRepositoryImpl: ReframeRepositoryImpl
    ): ReframeRepository
}
