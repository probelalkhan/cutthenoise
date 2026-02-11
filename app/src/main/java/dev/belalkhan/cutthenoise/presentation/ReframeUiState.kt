package dev.belalkhan.cutthenoise.presentation

import dev.belalkhan.cutthenoise.domain.model.Persona

sealed interface ReframeUiState {
    data object Idle : ReframeUiState

    data class Processing(
        val cards: List<PersonaCardUi>
    ) : ReframeUiState

    data class Error(val message: String) : ReframeUiState
}

data class PersonaCardUi(
    val persona: Persona,
    val content: String,
    val isGenerating: Boolean
)
