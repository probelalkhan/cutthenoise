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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.belalkhan.cutthenoise.R
import dev.belalkhan.cutthenoise.ui.theme.DarkCharcoal
import dev.belalkhan.cutthenoise.ui.theme.ElectricTeal
import dev.belalkhan.cutthenoise.ui.theme.NightBlack
import dev.belalkhan.cutthenoise.ui.theme.TextPrimary
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onReframeRequested: (String) -> Unit
) {
    val userInput by viewModel.userInput.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val recentReframes by viewModel.recentReframes.collectAsState()
    
    
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val drawerSearchQuery by viewModel.drawerSearchQuery.collectAsState()
    val historyResults by viewModel.historySearchResults.collectAsState()
    var showAboutDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    
    val inputFocusRequester = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }

    
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
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.about_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.version_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.close), color = ElectricTeal)
                }
            },
            containerColor = DarkCharcoal,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.material3.ModalDrawerSheet(
                drawerContainerColor = NightBlack,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp) 
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (drawerSearchQuery.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.search_hint),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary.copy(alpha = 0.5f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (drawerSearchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Search",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { viewModel.onDrawerSearchQueryChanged("") }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        
                        item {
                            Text(
                                text = stringResource(R.string.previous_reframes),
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
                                text = stringResource(R.string.about_title),
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0), 
        topBar = {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkCharcoal)
                        .clickable { 
                            scope.launch { drawerState.open() } 
                        },
                    contentAlignment = Alignment.Center
                ) {
                   Canvas(modifier = Modifier.size(18.dp)) {
                       val strokeWidth = 2.dp.toPx()
                       val width = size.width
                       val height = size.height
                       
                       
                       drawLine(
                           color = TextPrimary,
                           start = androidx.compose.ui.geometry.Offset(0f, height * 0.3f),
                           end = androidx.compose.ui.geometry.Offset(width, height * 0.3f),
                           strokeWidth = strokeWidth,
                           cap = androidx.compose.ui.graphics.StrokeCap.Round
                       )
                       
                       
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

                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp)) 
                        .background(DarkCharcoal) 
                        .padding(horizontal = 20.dp, vertical = 10.dp) 
                ) {
                   Text(
                       text = stringResource(R.string.app_name),
                       style = MaterialTheme.typography.titleMedium, 
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
                .padding(innerPadding) 
                .imePadding() 
        ) {
            
            Column(
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                 
                Image(
                    painter = painterResource(id = R.drawable.ic_ctn_logo),
                    contentDescription = "CutTheNoise Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                
                Text(
                    text = stringResource(R.string.punchline),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary.copy(alpha = 0.5f), 
                    textAlign = TextAlign.Center
                )
            }

            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() 
            ) {
                
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
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = TextPrimary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                
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
                         
                         Box(
                             modifier = Modifier
                                 .weight(1f)
                                 .padding(vertical = 12.dp, horizontal = 8.dp),
                             contentAlignment = Alignment.CenterStart
                         ) {
                             if (userInput.isEmpty() && !isListening) {
                                 Text(
                                     text = stringResource(R.string.input_placeholder), 
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
                                 maxLines = 4,
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .focusRequester(inputFocusRequester)
                             )
                         }
                         
                         Spacer(modifier = Modifier.width(4.dp))
                         
                         
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
