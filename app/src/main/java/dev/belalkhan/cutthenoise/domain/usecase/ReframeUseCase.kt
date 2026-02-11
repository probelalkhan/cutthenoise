package dev.belalkhan.cutthenoise.domain.usecase

import dev.belalkhan.cutthenoise.domain.model.Persona
import dev.belalkhan.cutthenoise.domain.model.PersonaCard
import dev.belalkhan.cutthenoise.domain.model.ReframeState
import dev.belalkhan.cutthenoise.domain.repository.LlmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

/**
 * Orchestrates reframing a thought through all three personas sequentially.
 * Emits [ReframeState] updates progressively so the UI can show each card
 * as it completes while the next one is still generating.
 */
class ReframeUseCase @Inject constructor(
    private val repository: LlmRepository
) {

    operator fun invoke(userInput: String): Flow<ReframeState> = flow {
        val completedCards = mutableListOf<PersonaCard>()

        emit(ReframeState.Processing(completedCards.toList()))

        for (persona in Persona.entries) {
            try {
                
                val contentBuilder = StringBuilder()
                repository.reframe(persona, userInput).collect { token ->
                    contentBuilder.append(token)
                    
                    val inProgressCard = PersonaCard(persona, contentBuilder.toString())
                    emit(
                        ReframeState.Processing(
                            completedCards + inProgressCard
                        )
                    )
                }

                
                completedCards.add(PersonaCard(persona, contentBuilder.toString()))
                emit(ReframeState.Processing(completedCards.toList()))
            } catch (e: Exception) {
                emit(ReframeState.Error("Failed to generate ${persona.title}: ${e.message}"))
                return@flow
            }
        }

        
        emit(ReframeState.Done(completedCards.toList()))
    }
}
