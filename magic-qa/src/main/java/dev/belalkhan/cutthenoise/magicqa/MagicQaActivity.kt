package dev.belalkhan.cutthenoise.magicqa

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MagicQaActivity : ComponentActivity() {

    private var state by mutableStateOf(QaState.IDLE)
    private var statusMessage by mutableStateOf("Ready to capture")
    private var streamerService: MagicStreamerService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as MagicStreamerService.LocalBinder
            streamerService = localBinder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            streamerService = null
            serviceBound = false
        }
    }

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startStreamerService(result.resultCode, result.data!!)
        } else {
            state = QaState.IDLE
            statusMessage = "Permission denied"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MagicQaScreen(
                        state = state,
                        statusMessage = statusMessage,
                        onStartCapture = ::requestMediaProjection,
                        onFinishCapture = ::finishCapture
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun requestMediaProjection() {
        state = QaState.REQUESTING_PERMISSION
        statusMessage = "Requesting screen capture permission..."
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun startStreamerService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, MagicStreamerService::class.java).apply {
            putExtra(MagicStreamerService.EXTRA_RESULT_CODE, resultCode)
            putExtra(MagicStreamerService.EXTRA_DATA, data)
        }
        startForegroundService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        state = QaState.CAPTURING
        statusMessage = "Capturing screen frames..."
    }

    private fun finishCapture() {
        state = QaState.GENERATING
        statusMessage = "Stopping capture & generating test..."
        streamerService?.stopCaptureAndGenerate { success, message ->
            runOnUiThread {
                state = if (success) QaState.DONE else QaState.ERROR
                statusMessage = message
            }
        }
    }
}

enum class QaState {
    IDLE,
    REQUESTING_PERMISSION,
    CAPTURING,
    GENERATING,
    DONE,
    ERROR
}

@Composable
fun MagicQaScreen(
    state: QaState,
    statusMessage: String,
    onStartCapture: () -> Unit,
    onFinishCapture: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Magic QA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF58A6FF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stream • Observe • Generate Tests",
                fontSize = 14.sp,
                color = Color(0xFF8B949E)
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Status indicator
            AnimatedVisibility(
                visible = state == QaState.CAPTURING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Color(0xFF1A1E24),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FiberManualRecord,
                        contentDescription = null,
                        tint = Color(0xFFFF4444),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORDING",
                        color = Color(0xFFFF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state == QaState.GENERATING) {
                CircularProgressIndicator(
                    color = Color(0xFF58A6FF),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status message
            Text(
                text = statusMessage,
                fontSize = 14.sp,
                color = Color(0xFFC9D1D9)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            when (state) {
                QaState.IDLE, QaState.ERROR, QaState.DONE -> {
                    Button(
                        onClick = onStartCapture,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF238636)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Capture",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                QaState.CAPTURING -> {
                    Button(
                        onClick = onFinishCapture,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDA3633)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finish & Generate",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> { /* REQUESTING_PERMISSION, GENERATING — no buttons */ }
            }
        }
    }
}
