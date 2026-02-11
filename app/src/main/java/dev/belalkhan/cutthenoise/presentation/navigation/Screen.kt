package dev.belalkhan.cutthenoise.presentation.navigation

sealed class Screen(val route: String) {
    data object Input : Screen("input")
    data object Result : Screen("result/{thought}") {
        fun createRoute(thought: String) = "result/$thought"
    }
}
