package dev.belalkhan.cutthenoise.data.repository

import dev.belalkhan.cutthenoise.data.local.llm.LlmInferenceSource
import dev.belalkhan.cutthenoise.domain.model.Persona
import dev.belalkhan.cutthenoise.domain.repository.LlmRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepositoryImpl @Inject constructor(
    private val llmInferenceSource: LlmInferenceSource
) : LlmRepository {

    override fun reframe(persona: Persona, userInput: String): Flow<String> = callbackFlow {
        val prompt = buildPrompt(persona, userInput)
        val accumulated = StringBuilder()

        llmInferenceSource.infer(prompt) { token ->
            accumulated.append(token)
            trySend(accumulated.toString())
        }

        
        channel.close()

        awaitClose()
    }

    private fun buildPrompt(persona: Persona, userInput: String): String {
        val systemPrompt = when (persona) {
            Persona.STOIC ->
                "You are Marcus Aurelius, the Stoic philosopher-emperor. " +
                "A person comes to you with a worry. Reframe their thought using Stoic principles: " +
                "focus on what is within their control, the impermanence of hardship, " +
                "and the strength found in acceptance. Be concise, wise, and calming. " +
                "Speak directly to them in 2-3 sentences."

            Persona.STRATEGIST ->
                "You are a brilliant strategic advisor, calm under pressure. " +
                "A person shares a stressful thought. Reframe it into an actionable plan: " +
                "break down the problem, identify the first concrete step, and show them " +
                "a path forward. Be practical and empowering. " +
                "Speak directly to them in 2-3 sentences."

            Persona.OPTIMIST ->
                "You are a warm, genuine optimist who sees silver linings. " +
                "A person shares something that worries them. Reframe their thought by " +
                "highlighting the hidden opportunity, the growth potential, or the " +
                "unexpected positive side. Be authentic, not dismissive of their feelings. " +
                "Speak directly to them in 2-3 sentences."
        }

        val sysOpen = "<" + "|system|" + ">"
        val userOpen = "<" + "|user|" + ">"
        val assistOpen = "<" + "|assistant|" + ">"
        val eos = "<" + "/s" + ">"
        val nl = "\n"
        return sysOpen + nl + systemPrompt + eos + nl +
               userOpen + nl + userInput + eos + nl +
               assistOpen + nl
    }
}
