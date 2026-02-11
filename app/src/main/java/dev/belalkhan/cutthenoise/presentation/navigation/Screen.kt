package dev.belalkhan.cutthenoise.presentation.navigation

sealed class Screen(val route: String) {
    data object ThoughtInput : Screen("thought_input")
    data object ReframeResult : Screen("reframe_result/{thought}") {
        fun createRoute(thought: String) = "reframe_result/$thought"
    }
}
