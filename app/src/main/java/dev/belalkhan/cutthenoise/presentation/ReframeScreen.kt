package dev.belalkhan.cutthenoise.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.belalkhan.cutthenoise.R
import dev.belalkhan.cutthenoise.presentation.components.InputSection
import dev.belalkhan.cutthenoise.presentation.components.ResultCarousel
import dev.belalkhan.cutthenoise.ui.theme.ElectricTeal
import dev.belalkhan.cutthenoise.ui.theme.NightBlack
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

        // Subtle top gradient glow
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.06f),
                                NightBlack
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_ctn_logo),
                    contentDescription = "CutTheNoise Logo",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reframe your stress through 3 perspectives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Input section
                val isLoading = uiState is ReframeUiState.Processing
                InputSection(
                    text = userInput,
                    onTextChanged = viewModel::onInputChanged,
                    onReframeClick = viewModel::reframe,
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Results
                val cards = when (val state = uiState) {
                    is ReframeUiState.Processing -> state.cards
                    else -> emptyList()
                }

                AnimatedVisibility(
                    visible = cards.isNotEmpty(),
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) { it / 3 }
                ) {
                    ResultCarousel(
                        cards = cards,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
