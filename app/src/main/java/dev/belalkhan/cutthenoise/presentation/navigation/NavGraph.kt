package dev.belalkhan.cutthenoise.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.belalkhan.cutthenoise.presentation.input.InputScreen
import dev.belalkhan.cutthenoise.presentation.input.InputViewModel
import dev.belalkhan.cutthenoise.presentation.result.ResultScreen
import dev.belalkhan.cutthenoise.presentation.result.ResultViewModel

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.ThoughtInput.route
    ) {
        composable(route = Screen.ThoughtInput.route) {
             InputScreen(
                viewModel = hiltViewModel(),
                onReframeRequested = { thought ->
                    navController.navigate(Screen.ReframeResult.createRoute(thought))
                }
            )
        }

        composable(
            route = Screen.ReframeResult.route,
            arguments = listOf(navArgument("thought") { type = NavType.StringType })
        ) {
            // We can scope a shared ViewModel to the nav graph if we wanted to share state
            // directly, but sticking to the plan of separate ViewModels.
            // ResultViewModel will need a way to get the data.
            // Ideally, we'd pass arguments, but for large data (cards), a shared ViewModel
            // scoped to the navigation graph (or activity) is often better.
            // For now, let's stick to the plan of separate screens.
            // To make this work seamlessly with the current architecture where ReframeViewModel held everything:
            // The InputViewModel will trigger the generation and hold the state.
            // Actually, per the user request, the Result screen needs to show the generated cards.
            // The easiest way without complex serialization is to share a ViewModel or
            // use a shared state holder.

            // Given the complexity of checking scoping in standard Hilt-Nav-Compose without a nested graph,
            // I'll create the ResultScreen and we can wire the data passing via a shared ViewModel or
            // by passing the user input and re-triggering (bad UX).

            // BETTER APPROACH for this constrained environment:
            // Use a SharedViewModel scoped to the Activity or a nested navigation graph.
            // Or simpler: InputViewModel triggers navigation, and we pass the
            // thought as an argument, and ResultViewModel does the generation?
            // "Once the action button is pressed, user will land to the next screen where we show 3 cards."
            // This implies the generation happens ON the results screen or strictly before.
            // If it happens on Result screen, we just pass the "thought" string.
            // That's much cleaner.

            val viewModel: ResultViewModel = hiltViewModel()
            ResultScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
