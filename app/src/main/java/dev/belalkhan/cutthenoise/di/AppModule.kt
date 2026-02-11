package dev.belalkhan.cutthenoise.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.belalkhan.cutthenoise.data.local.db.CutTheNoiseDatabase
import dev.belalkhan.cutthenoise.data.local.db.ReframeDao
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CutTheNoiseDatabase {
        return Room.databaseBuilder(
            context,
            CutTheNoiseDatabase::class.java,
            "cut_the_noise.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideReframeDao(database: CutTheNoiseDatabase): ReframeDao {
        return database.reframeDao()
    }
}
