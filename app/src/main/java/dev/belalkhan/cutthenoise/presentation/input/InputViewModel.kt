package dev.belalkhan.cutthenoise.presentation.input

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor() : ViewModel() {

    private val _userInput = MutableStateFlow("")
    val userInput = _userInput.asStateFlow()

    fun onInputChanged(input: String) {
        _userInput.value = input
    }
}
