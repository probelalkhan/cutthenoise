package dev.belalkhan.cutthenoise.domain

data class SentenceAnalysis(
    val sentence: String,
    val actionable: Boolean,
    val hasOwner: Boolean,
    val hasTimeline: Boolean,
    val hasMeasurableOutcome: Boolean,
    val problems: List<Problem>,
    val explanation: String
)

enum class Problem {
    VAGUE_ACTION,
    NO_OWNER,
    NO_TIMELINE,
    NO_MEASUREMENT,
    BUZZWORD_OVERUSE,
    PASSIVE_LANGUAGE
}

data class NoiseAnalysisResult(
    val totalSentences: Int,
    val nonActionableCount: Int,
    val analyses: List<SentenceAnalysis>
)
