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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch
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
    
    // Drawer & Search States
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val drawerSearchQuery by viewModel.drawerSearchQuery.collectAsState()
    val historyResults by viewModel.historySearchResults.collectAsState()
    var showAboutDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Focus Requester to focus input when "Edit" (New Chat) is clicked
    val inputFocusRequester = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }

    // Permission launcher for voice input
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    if (showAboutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "About CutTheNoise",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "CutTheNoise helps you reframe your stress through three powerful perspectives: Stoic, Strategist, and Optimist. Gain clarity, reduce stress, and find actionable solutions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = ElectricTeal)
                }
            },
            containerColor = DarkCharcoal,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    androidx.compose.material3.ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.material3.ModalDrawerSheet(
                drawerContainerColor = NightBlack,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp) // Fixed width for drawer
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Section: Search & Edit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        BasicTextField(
                            value = drawerSearchQuery,
                            onValueChange = viewModel::onDrawerSearchQueryChanged,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                            singleLine = true,
                            cursorBrush = SolidColor(ElectricTeal),
                            decorationBox = { innerTextField ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(DarkCharcoal, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box {
                                        if (drawerSearchQuery.isEmpty()) {
                                            Text(
                                                text = "Search",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary.copy(alpha = 0.5f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))

                        // Edit / New Chat Button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    // Slight delay to allow drawer close animation to start
                                    kotlinx.coroutines.delay(100) 
                                    inputFocusRequester.requestFocus()
                                }
                            }
                        ) {
                           Icon(
                               imageVector = Icons.Default.Edit,
                               contentDescription = "New Chat",
                               tint = TextPrimary
                           )
                        }
                    }

                    // History List (Paginated / LazyColumn)
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section Header
                        item {
                            Text(
                                text = "Previous Reframes",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(historyResults) { item ->
                            Text(
                                text = item.thought,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onReframeRequested("id:${item.id}")
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Sticky About Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAboutDialog = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About",
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "About CutTheNoise",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    ) {

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
                        .clickable { 
                            scope.launch { drawerState.open() } // Open drawer
                        },
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
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .focusRequester(inputFocusRequester)
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
}
