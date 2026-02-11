package dev.belalkhan.cutthenoise.domain.model

enum class Persona(
    val title: String,
    val tagline: String,
    val icon: String,
    val accentColor: Long
) {
    STOIC(
        title = "The Stoic",
        tagline = "What is within your control?",
        icon = "🏛️",
        accentColor = 0xFF7E8AA2
    ),
    STRATEGIST(
        title = "The Strategist",
        tagline = "What is the actionable plan?",
        icon = "♟️",
        accentColor = 0xFF2ECC71
    ),
    OPTIMIST(
        title = "The Optimist",
        tagline = "What is the hidden opportunity?",
        icon = "☀️",
        accentColor = 0xFFF39C12
    )
}
