package dev.belalkhan.cutthenoise.presentation.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.cutthenoise.domain.model.Persona
import dev.belalkhan.cutthenoise.domain.model.ReframeState
import dev.belalkhan.cutthenoise.domain.repository.ReframeRepository
import dev.belalkhan.cutthenoise.domain.usecase.ReframeUseCase
import dev.belalkhan.cutthenoise.presentation.PersonaCardUi
import dev.belalkhan.cutthenoise.presentation.ReframeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reframeUseCase: ReframeUseCase,
    private val repository: ReframeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReframeUiState>(ReframeUiState.Idle)
    val uiState: StateFlow<ReframeUiState> = _uiState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    // The thought passed from the previous screen
    val thought: String = checkNotNull(savedStateHandle["thought"])

    init {
        generateReframe()
    }

    private fun generateReframe() {
        viewModelScope.launch {
            reframeUseCase(thought).collect { state ->
                _uiState.value = mapToUiState(state)
            }
        }
    }

    fun saveResult() {
        val currentState = _uiState.value
        if (currentState is ReframeUiState.Done && !_isSaved.value) {
            viewModelScope.launch {
                val cards = currentState.cards
                // We assume the order is Stoic, Strategist, Optimist based on the enum order
                // or we can find them by persona type to be safe.
                val stoic = cards.find { it.persona == Persona.STOIC }?.content ?: ""
                val strategist = cards.find { it.persona == Persona.STRATEGIST }?.content ?: ""
                val optimist = cards.find { it.persona == Persona.OPTIMIST }?.content ?: ""

                repository.saveReframe(
                    thought = thought,
                    stoicResponse = stoic,
                    strategistResponse = strategist,
                    optimistResponse = optimist
                )
                _isSaved.value = true
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
