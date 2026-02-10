package dev.belalkhan.cutthenoise.service

import dev.belalkhan.cutthenoise.domain.NoiseAnalysisResult
import dev.belalkhan.cutthenoise.domain.Problem
import dev.belalkhan.cutthenoise.domain.SentenceAnalysis
import dev.belalkhan.cutthenoise.llm.LocalLlmEngine

class DefaultNoiseAnalysisService(
    private val splitter: SentenceSplitter,
    private val llm: LocalLlmEngine
) : NoiseAnalysisService {

    override suspend fun analyze(text: String): NoiseAnalysisResult {
        val sentences = splitter.split(text)

        val analyses = sentences.map { sentence ->
            val prompt = PromptFactory.forSentence(sentence)
            val rawJson = llm.infer(prompt)
            JsonParser.parseSentenceAnalysis(sentence, rawJson)
        }

        return NoiseAnalysisResult(
            totalSentences = analyses.size,
            nonActionableCount = analyses.count { !it.actionable },
            analyses = analyses
        )
    }
}

object JsonParser {

    fun parseSentenceAnalysis(
        sentence: String,
        json: String
    ): SentenceAnalysis {
        // 1. Try parse
        // 2. If fails → fallback classification
        // 3. Never crash

        TODO()
    }
}

object BsHeuristics {

    private val buzzwords = setOf(
        "align", "synergy", "leverage", "bandwidth",
        "explore", "discuss", "circle back"
    )

    fun detect(sentence: String): Set<Problem> {
        val found = mutableSetOf<Problem>()
        val lower = sentence.lowercase()

        if (buzzwords.any { it in lower }) {
            found.add(Problem.BUZZWORD_OVERUSE)
        }

        if (lower.startsWith("we will") && !lower.contains("by")) {
            found.add(Problem.NO_TIMELINE)
        }

        return found
    }
}

object PromptFactory {

    fun forSentence(sentence: String): String {
        return """
            You are a strict meeting notes reviewer.
            Your job is to analyze the sentence below.

            RULES:
            - Do not rewrite the sentence
            - Do not add suggestions
            - Do not add extra fields
            - Respond with JSON ONLY
            - Be strict and literal

            Sentence:
            "$sentence"

            Return JSON in this exact format:

            {
              "actionable": true or false,
              "hasOwner": true or false,
              "hasTimeline": true or false,
              "hasMeasurableOutcome": true or false,
              "problems": [
                "VAGUE_ACTION",
                "NO_OWNER",
                "NO_TIMELINE",
                "NO_MEASUREMENT",
                "BUZZWORD_OVERUSE",
                "PASSIVE_LANGUAGE"
              ],
              "explanation": "one short sentence explanation"
            }
        """.trimIndent()
    }
}


