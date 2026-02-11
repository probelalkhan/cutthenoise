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
            
            
            
            
            
            
            
            
            
            
            

            
            
            

            
            
            
            
            
            
            
            

            val viewModel: ResultViewModel = hiltViewModel()
            ResultScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
