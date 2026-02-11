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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Disable Scaffold default insets to handle manually
        topBar = {
            // Custom Toolbar matching design structure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Navigation Drawer Button (Circle with 2 lines)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkCharcoal)
                        .clickable { /* No drawer request in refined plan */ },
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

                Spacer(modifier = Modifier.width(12.dp))

                // "CutTheNoise" Pill Label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp)) // More rounded pill shape
                        .background(DarkCharcoal) // Matching button bg 
                        .padding(horizontal = 20.dp, vertical = 10.dp) // Generous padding like in image
                ) {
                   Text(
                       text = "CutTheNoise",
                       style = MaterialTheme.typography.titleMedium, // Slightly larger
                       color = TextPrimary,
                       fontWeight = FontWeight.SemiBold
                   )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // This applies Top Bar padding only due to contentWindowInsets(0)
                .imePadding() // Animate entire screen content effectively pushing bottom element up
        ) {
            // Center Content (Logo + Punchline only)
            Column(
                modifier = Modifier
                    .weight(1f) // Takes available space
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                 // Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_ctn_logo),
                    contentDescription = "CutTheNoise Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Punchline (Increased size)
                Text(
                    text = "Reframe your stress\nthrough 3 perspectives",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary.copy(alpha = 0.5f), // Increased alpha
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Section (Recent Cards + Input)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Ensure it stays above nav bar
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
                                        style = MaterialTheme.typography.bodyMedium, // Increased from bodySmall
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
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
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
                                 .background(
                                     if (isListening) ElectricTeal.copy(alpha = 0.2f) else Color.Transparent,
                                     CircleShape
                                 )
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
                                 .background(
                                     if (isSendEnabled) ElectricTeal else Color.Transparent,
                                     CircleShape
                                 )
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
