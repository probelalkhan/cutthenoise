package dev.belalkhan.cutthenoise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.belalkhan.cutthenoise.presentation.ReframeScreen
import dev.belalkhan.cutthenoise.presentation.ReframeViewModel
import dev.belalkhan.cutthenoise.ui.theme.CutTheNoiseTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CutTheNoiseTheme {
                val viewModel: ReframeViewModel = hiltViewModel()
                ReframeScreen(viewModel = viewModel)
            }
        }
    }
}