package dev.belalkhan.cutthenoise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import dev.belalkhan.cutthenoise.llm.NativeLlmEngine

class AnalyzerViewModel(
    private val llmEngine: NativeLlmEngine
) : ViewModel() {

    private val tag = "CutTheNoiseXX"

    // A realistic snippet of a "noisy" meeting
    private val meetingNotes = listOf(
        "We need to explore potential synergies to leverage our bandwidth effectively.", // Text 1: High noise
        "Rahul will fix the login bug by 5 PM today.",                                 // Text 2: Good/Actionable
        "Let's align on the roadmap and circle back next week."                       // Text 3: Vague
    )

    init {
        runDemoAnalysis()
    }

    private fun runDemoAnalysis() {
        Log.d(tag, ">>> STARTING CUT THE NOISE ANALYSIS <<<")

        viewModelScope.launch {
            meetingNotes.forEachIndexed { index, note ->

                // 1. Construct the Analysis Prompt
                val prompt = buildAnalysisPrompt(note)

                Log.d(tag, "--------------------------------------------------")
                Log.d(tag, "ANALYZING BLOCK #${index + 1}: \"$note\"")

                val resultBuffer = StringBuilder()

                // 2. Stream the Analysis
                val finalResult = withContext(Dispatchers.IO) {
                    llmEngine.infer(prompt) { token ->
                        // Optional: Log token if you want to see speed
                        // Log.v(tag, "Stream: $token")
                        resultBuffer.append(token)
                    }
                }

                // 3. Log the Final "Smart" Report
                Log.d(tag, "\n--- 🤖 AI REPORT ---")
                Log.d(tag, finalResult.trim())
                Log.d(tag, "--------------------------------------------------\n")
            }
        }
    }

    private fun buildAnalysisPrompt(input: String): String {
        // TinyLlama Instruction Template
        // We give it specific criteria for "Actionable" vs "BS Detection"
        return """
        <|system|>
        You are "CutTheNoise", a ruthless editor. Analyze the user's meeting note.
        
        Rules:
        1. Classify if Actionable (Yes/No).
        2. Identify Owner and Timeline (if missing, say None).
        3. Flag "BS words" (vague verbs like 'align', 'explore' or buzzwords like 'synergy', 'leverage').
        4. Explain the issue simply.
        
        Output format:
        Actionable: [Yes/No]
        Owner: [Name]
        Timeline: [Time]
        BS Flags: [List words]
        Issue: [Brief explanation]</s>
        
        <|user|>
        $input</s>
        
        <|assistant|>
        """.trimIndent()
    }
}