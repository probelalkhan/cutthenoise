package dev.belalkhan.cutthenoise.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.cutthenoise.domain.model.Persona
import dev.belalkhan.cutthenoise.domain.model.ReframeState
import dev.belalkhan.cutthenoise.domain.usecase.ReframeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReframeViewModel @Inject constructor(
    private val reframeUseCase: ReframeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReframeUiState>(ReframeUiState.Idle)
    val uiState: StateFlow<ReframeUiState> = _uiState.asStateFlow()

    val userInput = MutableStateFlow("")

    fun onInputChanged(input: String) {
        userInput.value = input
    }

    fun reframe() {
        val input = userInput.value.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            reframeUseCase(input).collect { state ->
                _uiState.value = mapToUiState(state)
            }
        }
    }

    private fun mapToUiState(state: ReframeState): ReframeUiState {
        return when (state) {
            is ReframeState.Idle -> ReframeUiState.Idle

            is ReframeState.Processing -> {
                val totalPersonas = Persona.entries.size
                val cards = state.completedCards.mapIndexed { index, card ->
                    val isLast = index == state.completedCards.lastIndex
                    // Only mark as generating if this is the last card AND
                    // we haven't completed all personas yet
                    val isStillStreaming = isLast && state.completedCards.size < totalPersonas
                    PersonaCardUi(
                        persona = card.persona,
                        content = card.content,
                        isGenerating = isStillStreaming
                    )
                }
                ReframeUiState.Processing(cards = cards)
            }

            is ReframeState.Done -> {
                val cards = state.completedCards.map { card ->
                    PersonaCardUi(
                        persona = card.persona,
                        content = card.content,
                        isGenerating = false
                    )
                }
                ReframeUiState.Done(cards = cards)
            }

            is ReframeState.Error -> ReframeUiState.Error(state.message)
        }
    }
}
