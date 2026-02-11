package dev.belalkhan.cutthenoise.domain.model

sealed interface ReframeState {
    data object Idle : ReframeState
    data class Processing(val completedCards: List<PersonaCard>) : ReframeState
    data class Error(val message: String) : ReframeState
}
