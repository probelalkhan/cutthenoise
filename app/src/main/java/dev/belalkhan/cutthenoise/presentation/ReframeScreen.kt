package dev.belalkhan.cutthenoise.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.belalkhan.cutthenoise.presentation.components.InputSection
import dev.belalkhan.cutthenoise.presentation.components.ResultCarousel
import dev.belalkhan.cutthenoise.ui.theme.ElectricTeal
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

@Composable
fun ReframeScreen(
    viewModel: ReframeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in snackbar
    LaunchedEffect(uiState) {
        if (uiState is ReframeUiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as ReframeUiState.Error).message
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                        append("Cut")
                    }
                    withStyle(SpanStyle(color = ElectricTeal, fontWeight = FontWeight.ExtraBold)) {
                        append("The")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                        append("Noise")
                    }
                },
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reframe your stress through 3 perspectives",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Input section
            val isLoading = uiState is ReframeUiState.Processing
            InputSection(
                text = userInput,
                onTextChanged = viewModel::onInputChanged,
                onReframeClick = viewModel::reframe,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Results
            val cards = when (val state = uiState) {
                is ReframeUiState.Processing -> state.cards
                else -> emptyList()
            }

            AnimatedVisibility(
                visible = cards.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                ResultCarousel(
                    cards = cards,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
