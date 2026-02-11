package dev.belalkhan.cutthenoise.presentation.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // The thought text, updated either from args or DB
    var thought by mutableStateOf("")
        private set

    init {
        val rawInput = checkNotNull(savedStateHandle.get<String>("thought"))
        
        if (rawInput.startsWith("id:")) {
            val id = rawInput.removePrefix("id:").toLongOrNull()
            if (id != null) {
                loadReframe(id)
            } else {
                _uiState.value = ReframeUiState.Error("Invalid Request ID")
            }
        } else {
            thought = rawInput
            generateReframe()
        }
    }

    private fun loadReframe(id: Long) {
        viewModelScope.launch {
            val entity = repository.getReframeById(id)
            if (entity != null) {
                thought = entity.thought
                _isSaved.value = true
                
                // Create Done state with no animation
                val cards = listOf(
                    PersonaCardUi(Persona.STOIC, entity.stoicResponse, isGenerating = false, shouldAnimate = false),
                    PersonaCardUi(Persona.STRATEGIST, entity.strategistResponse, isGenerating = false, shouldAnimate = false),
                    PersonaCardUi(Persona.OPTIMIST, entity.optimistResponse, isGenerating = false, shouldAnimate = false)
                )
                _uiState.value = ReframeUiState.Done(cards)
            } else {
                _uiState.value = ReframeUiState.Error("Reframe history not found")
            }
        }
    }

    private fun generateReframe() {
        if (thought.isBlank()) return
        
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
                    val isStillStreaming = isLast && state.completedCards.size < totalPersonas
                    PersonaCardUi(
                        persona = card.persona,
                        content = card.content,
                        isGenerating = isStillStreaming,
                        shouldAnimate = true // Always animate for new generation
                    )
                }
                ReframeUiState.Processing(cards = cards)
            }

            is ReframeState.Done -> {
                val cards = state.completedCards.map { card ->
                    PersonaCardUi(
                        persona = card.persona,
                        content = card.content,
                        isGenerating = false,
                        shouldAnimate = true // Maintained for consistency if verifying
                    )
                }
                ReframeUiState.Done(cards = cards)
            }

            is ReframeState.Error -> ReframeUiState.Error(state.message)
        }
    }
}
