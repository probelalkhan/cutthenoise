package dev.belalkhan.cutthenoise.presentation.input

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.belalkhan.cutthenoise.R
import dev.belalkhan.cutthenoise.ui.theme.DarkCharcoal
import dev.belalkhan.cutthenoise.ui.theme.ElectricTeal
import dev.belalkhan.cutthenoise.ui.theme.NightBlack
import dev.belalkhan.cutthenoise.ui.theme.TextPrimary
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onReframeRequested: (String) -> Unit
) {
    val userInput by viewModel.userInput.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val recentReframes by viewModel.recentReframes.collectAsState()
    
    // Permission launcher for voice input
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Custom Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Navigation Drawer Button (Circle with 2 lines)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkCharcoal)
                        .clickable { /* No drawer request in "Refined" plan, but button required visually */ },
                    contentAlignment = Alignment.Center
                ) {
                   Canvas(modifier = Modifier.size(18.dp)) {
                       val strokeWidth = 2.dp.toPx()
                       val width = size.width
                       val height = size.height
                       
                       // Top line (longer)
                       drawLine(
                           color = TextPrimary,
                           start = androidx.compose.ui.geometry.Offset(0f, height * 0.3f),
                           end = androidx.compose.ui.geometry.Offset(width, height * 0.3f),
                           strokeWidth = strokeWidth,
                           cap = androidx.compose.ui.graphics.StrokeCap.Round
                       )
                       
                       // Bottom line (shorter)
                       drawLine(
                           color = TextPrimary,
                           start = androidx.compose.ui.geometry.Offset(0f, height * 0.7f),
                           end = androidx.compose.ui.geometry.Offset(width * 0.6f, height * 0.7f),
                           strokeWidth = strokeWidth,
                           cap = androidx.compose.ui.graphics.StrokeCap.Round
                       )
                   }
                }

                // "CutTheNoise" Label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NightBlack) // Slightly lighter than bg? Or DarkCharcoal
                         .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                   Text(
                       text = "CutTheNoise",
                       style = MaterialTheme.typography.labelLarge,
                       color = TextPrimary.copy(alpha = 0.8f),
                       fontWeight = FontWeight.Bold
                   )
                }

                // Spacer to balance the layout (optional, or maybe profile icon? User didn't ask)
                Spacer(modifier = Modifier.size(40.dp)) 
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Center Content (Logo + Text) - Weight 1 to push bottom bar down
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                 // Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_ctn_logo),
                    contentDescription = "CutTheNoise Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App Title
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append("Cut")
                        }
                        withStyle(SpanStyle(color = ElectricTeal, fontWeight = FontWeight.ExtraBold)) {
                            append("The")
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append("Noise")
                        }
                    },
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reframe your stress through 3 perspectives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Section (Recent Cards + Input)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Recent Cards
                if (recentReframes.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(recentReframes) { reframe ->
                            Card(
                                onClick = { onReframeRequested("id:${reframe.id}") },
                                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(100.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = reframe.thought,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(DarkCharcoal)
                        .padding(horizontal = 6.dp, vertical = 6.dp) 
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         // Voice Button
                         IconButton(
                             onClick = {
                                 if (isListening) viewModel.stopListening()
                                 else permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                             },
                             modifier = Modifier
                                 .size(40.dp)
                                 .background(if (isListening) ElectricTeal.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
                         ) {
                             Icon(
                                 imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                 contentDescription = "Voice Input",
                                 tint = if (isListening) ElectricTeal else TextSecondary
                             )
                         }
                         
                         Spacer(modifier = Modifier.width(4.dp))
                         
                         // Text Input
                         Box(
                             modifier = Modifier
                                 .weight(1f)
                                 .padding(vertical = 12.dp),
                             contentAlignment = Alignment.CenterStart
                         ) {
                             if (userInput.isEmpty() && !isListening) {
                                 Text(
                                     text = "Message CutTheNoise", 
                                     color = TextSecondary.copy(alpha = 0.5f),
                                     style = MaterialTheme.typography.bodyLarge
                                 )
                             }
                             BasicTextField(
                                 value = userInput,
                                 onValueChange = viewModel::onInputChanged,
                                 textStyle = TextStyle(
                                     color = TextPrimary,
                                     fontSize = 16.sp
                                 ),
                                 cursorBrush = SolidColor(ElectricTeal),
                                 maxLines = 1,
                                 singleLine = true,
                                 modifier = Modifier.fillMaxWidth()
                             )
                         }
                         
                         Spacer(modifier = Modifier.width(4.dp))
                         
                         // Send Button
                         val isSendEnabled = userInput.isNotBlank()
                         IconButton(
                             onClick = { if (isSendEnabled) onReframeRequested(userInput) },
                             enabled = isSendEnabled,
                             modifier = Modifier
                                 .size(40.dp)
                                 .background(if (isSendEnabled) ElectricTeal else Color.Transparent, CircleShape)
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ArrowUpward,
                                 contentDescription = "Send",
                                 tint = if (isSendEnabled) NightBlack else TextSecondary.copy(alpha = 0.5f)
                             )
                         }
                    }
                }
            }
        }
    }
}
