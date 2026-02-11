package dev.belalkhan.cutthenoise.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay

/**
 * A composable that reveals [fullText] character-by-character with a blinking cursor
 * at the end while animating. Uses [rememberSaveable] so the animation state
 * survives recomposition from pager swipes — once text is fully revealed, it
 * won't re-animate.
 *
 * @param fullText The complete text to reveal
 * @param isAnimating Whether this card is actively streaming
 * @param charDelayMs Delay between each character reveal in milliseconds
 */
@Composable
fun TypewriterText(
    fullText: String,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    charDelayMs: Long = 25L,
    shouldAnimate: Boolean = true
) {
    val targetCount = fullText.length
    var visibleCharCount by rememberSaveable { 
        mutableIntStateOf(if (shouldAnimate) 0 else targetCount) 
    }
    var hasCompletedOnce by rememberSaveable { mutableStateOf(!shouldAnimate) }

    // If the text was already fully revealed once, show it instantly
    if (hasCompletedOnce && visibleCharCount < targetCount) {
        visibleCharCount = targetCount
    }

    // Animate character count towards the full text length
    LaunchedEffect(fullText) {
        while (visibleCharCount < targetCount) {
            visibleCharCount++
            delay(charDelayMs)
        }
        // Mark this text as having completed its animation
        if (visibleCharCount >= targetCount && targetCount > 0) {
            hasCompletedOnce = true
        }
    }

    // Blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    val displayText = buildAnnotatedString {
        val visibleText = fullText.take(visibleCharCount)
        withStyle(SpanStyle(color = textColor)) {
            append(visibleText)
        }

        // Blinking cursor only while actively generating and not fully revealed
        if (isAnimating && visibleCharCount < targetCount) {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha)
                )
            ) {
                append("▎")
            }
        }
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}
