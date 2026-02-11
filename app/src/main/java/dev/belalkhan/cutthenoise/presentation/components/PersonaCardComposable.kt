package dev.belalkhan.cutthenoise.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.belalkhan.cutthenoise.presentation.PersonaCardUi
import dev.belalkhan.cutthenoise.ui.theme.DarkCharcoal
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

/**
 * A premium-looking card displaying a persona's reframing of the user's thought.
 * Features a colored left accent bar, persona icon, tagline, and typewriter text.
 * Uses IntrinsicSize to make the accent bar match the content height.
 */
@Composable
fun PersonaCardComposable(
    cardUi: PersonaCardUi,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(cardUi.persona.accentColor)
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        accentColor.copy(alpha = 0.1f)
                    )
                ),
                shape = cardShape
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkCharcoal,
                        DarkCharcoal.copy(alpha = 0.95f),
                        Color(0xFF161616)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cardUi.persona.icon,
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = cardUi.persona.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = cardUi.persona.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.4f),
                                    accentColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(18.dp))

                
                if (cardUi.content.isNotBlank()) {
                    TypewriterText(
                        fullText = cardUi.content.trim(),
                        isAnimating = cardUi.isGenerating,
                        shouldAnimate = cardUi.shouldAnimate,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (cardUi.isGenerating) {
                    Text(
                        text = "Contemplating...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
