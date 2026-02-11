package dev.belalkhan.cutthenoise.domain.repository

import dev.belalkhan.cutthenoise.domain.model.Persona
import kotlinx.coroutines.flow.Flow

interface LlmRepository {
    /**
     * Sends a prompt for the given [persona] to reframe the [userInput].
     * Emits accumulated text token-by-token as a [Flow].
     */
    fun reframe(persona: Persona, userInput: String): Flow<String>
}
