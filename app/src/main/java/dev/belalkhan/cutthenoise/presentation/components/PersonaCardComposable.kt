package dev.belalkhan.cutthenoise.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.belalkhan.cutthenoise.domain.model.Persona
import dev.belalkhan.cutthenoise.presentation.PersonaCardUi
import dev.belalkhan.cutthenoise.ui.theme.SurfaceElevated
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

/**
 * A premium-looking card displaying a persona's reframing of the user's thought.
 * Features a colored left accent bar, persona icon, and typewriter text effect.
 */
@Composable
fun PersonaCardComposable(
    cardUi: PersonaCardUi,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(cardUi.persona.accentColor)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Persona Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cardUi.persona.icon,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = cardUi.persona.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = cardUi.persona.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Typewriter content
                if (cardUi.content.isNotBlank()) {
                    TypewriterText(
                        fullText = cardUi.content.trim(),
                        isAnimating = cardUi.isGenerating,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (cardUi.isGenerating) {
                    Text(
                        text = "Thinking...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
