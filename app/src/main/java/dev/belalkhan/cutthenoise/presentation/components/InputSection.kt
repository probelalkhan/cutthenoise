package dev.belalkhan.cutthenoise.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.belalkhan.cutthenoise.ui.theme.DarkCharcoal
import dev.belalkhan.cutthenoise.ui.theme.ElectricTeal
import dev.belalkhan.cutthenoise.ui.theme.TextOnAccent
import dev.belalkhan.cutthenoise.ui.theme.TextPrimary
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

/**
 * Input section with a sleek text field and "Reframe" action button.
 */
@Composable
fun InputSection(
    text: String,
    onTextChanged: (String) -> Unit,
    onReframeClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "What's on your mind? Share a stressful thought...",
                    color = TextSecondary
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCharcoal,
                unfocusedContainerColor = DarkCharcoal,
                focusedBorderColor = ElectricTeal,
                unfocusedBorderColor = ElectricTeal.copy(alpha = 0.3f),
                cursorColor = ElectricTeal
            ),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReframeClick,
            enabled = text.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricTeal,
                contentColor = TextOnAccent,
                disabledContainerColor = ElectricTeal.copy(alpha = 0.3f),
                disabledContentColor = TextOnAccent.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TextOnAccent,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Reframe My Thought",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
