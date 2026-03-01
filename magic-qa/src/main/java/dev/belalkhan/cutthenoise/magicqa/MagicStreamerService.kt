package dev.belalkhan.cutthenoise.magicqa

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.firestore
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MagicStreamerService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
        private const val TAG = "MagicStreamer"
        private const val CHANNEL_ID = "magic_qa_channel"
        private const val NOTIFICATION_ID = 1001
        private const val CAPTURE_INTERVAL_MS = 500L // ~2 fps
        private const val MAX_FRAMES = 60 // Max frames to buffer (30 seconds at 2fps)
        private const val FRAME_WIDTH = 540 // Downscaled for API efficiency
        private const val FRAME_HEIGHT = 960
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var captureJob: Job? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val frameBuffer = mutableListOf<ByteArray>()
    private var isCapturing = false

    inner class LocalBinder : Binder() {
        fun getService(): MagicStreamerService = this@MagicStreamerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Starting capture...")
        startForeground(NOTIFICATION_ID, notification)

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val data: Intent? = intent?.getParcelableExtra(EXTRA_DATA, Intent::class.java)

        if (resultCode != -1 && data != null) {
            startCapture(resultCode, data)
        } else {
            Log.e(TAG, "Invalid MediaProjection result")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopCapture()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        val projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        imageReader = ImageReader.newInstance(
            FRAME_WIDTH,
            FRAME_HEIGHT,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "MagicQaCapture",
            FRAME_WIDTH,
            FRAME_HEIGHT,
            getScreenDensity(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        isCapturing = true
        captureJob = serviceScope.launch {
            while (isActive && isCapturing) {
                captureFrame()
                delay(CAPTURE_INTERVAL_MS)
            }
        }

        updateNotification("Capturing screen...")
        Log.d(TAG, "Screen capture started")
    }

    private fun captureFrame() {
        val image = imageReader?.acquireLatestImage() ?: return
        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * FRAME_WIDTH

            val bitmap = Bitmap.createBitmap(
                FRAME_WIDTH + rowPadding / pixelStride,
                FRAME_HEIGHT,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Crop to actual size if needed
            val croppedBitmap = if (bitmap.width != FRAME_WIDTH) {
                Bitmap.createBitmap(bitmap, 0, 0, FRAME_WIDTH, FRAME_HEIGHT).also {
                    bitmap.recycle()
                }
            } else {
                bitmap
            }

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            croppedBitmap.recycle()

            val jpegBytes = outputStream.toByteArray()

            synchronized(frameBuffer) {
                if (frameBuffer.size >= MAX_FRAMES) {
                    frameBuffer.removeAt(0) // Drop oldest frame
                }
                frameBuffer.add(jpegBytes)
            }
        } finally {
            image.close()
        }
    }

    private fun stopCapture() {
        isCapturing = false
        captureJob?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    fun stopCaptureAndGenerate(callback: (Boolean, String) -> Unit) {
        val frames: List<ByteArray>
        synchronized(frameBuffer) {
            frames = frameBuffer.toList()
        }
        stopCapture()
        updateNotification("Generating test with Gemini...")

        if (frames.isEmpty()) {
            callback(false, "No frames captured")
            stopSelf()
            return
        }

        serviceScope.launch {
            try {
                val result = generateTestWithGemini(frames)
                pushToFirestore(result)
                callback(true, "Test generated & pushed to Firestore!")
            } catch (e: Exception) {
                Log.e(TAG, "Generation failed", e)
                callback(false, "Error: ${e.message}")
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun generateTestWithGemini(frames: List<ByteArray>): Map<String, String> {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")

        // Sample frames evenly (send at most 10 to stay within limits)
        val sampledFrames = if (frames.size > 10) {
            val step = frames.size / 10
            (0 until 10).map { frames[it * step] }
        } else {
            frames
        }

        val prompt = content {
            sampledFrames.forEach { jpegBytes ->
                blob("image/jpeg", jpegBytes)
            }
            text(buildPrompt())
        }

        val response = model.generateContent(prompt)
        val responseText = response.text ?: throw IllegalStateException("Empty Gemini response")
        Log.d(TAG, "Gemini response: $responseText")

        return parseJsonResponse(responseText)
    }

    private fun buildPrompt(): String = """
        You are an Android test generation agent. You just observed a sequence of screen 
        captures from an Android app called "CutTheNoise" with base package 
        "dev.belalkhan.cutthenoise".
        
        The app uses Jetpack Compose and follows Clean Architecture with these packages:
        - presentation/input: InputScreen (thought input)
        - presentation/result: ResultScreen (reframed result)
        - presentation/components: UI components
        - presentation/navigation: NavGraph with Screen routes
        
        Generate a Compose UI test using the Robot Pattern:
        1. Create a Robot class that encapsulates all UI interactions (finding nodes, 
           clicking, typing, asserting)
        2. Create a Test class that uses the Robot to describe the user flow in 
           readable steps
        3. Use ComposeTestRule, semantic matchers, and proper test annotations
        
        Return ONLY valid JSON (no markdown fences) in this exact format:
        {
            "pkg": "dev.belalkhan.cutthenoise.presentation.input",
            "file": "InputScreenFlowTest.kt",
            "code": "package dev.belalkhan.cutthenoise.presentation.input\n\n..."
        }
        
        Rules:
        - pkg must be the actual detected package of the screen being tested
        - The Robot class should be in the same file, above the test class
        - Use @get:Rule val composeTestRule = createComposeRule()
        - Import from androidx.compose.ui.test.*
        - Make the test realistic based on what you see in the screenshots
        - Include proper assertions that verify UI state changes
    """.trimIndent()

    private fun parseJsonResponse(response: String): Map<String, String> {
        // Strip markdown code fences if present
        val cleaned = response
            .replace("```json", "")
            .replace("```", "")
            .trim()

        // Simple JSON parsing without external library
        val pkg = extractJsonField(cleaned, "pkg")
        val file = extractJsonField(cleaned, "file")
        val code = extractJsonCodeField(cleaned)

        return mapOf("pkg" to pkg, "file" to file, "code" to code)
    }

    private fun extractJsonField(json: String, field: String): String {
        val pattern = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
            ?: throw IllegalStateException("Missing '$field' in response")
    }

    private fun extractJsonCodeField(json: String): String {
        // The "code" field contains escaped newlines and quotes
        val startMarker = """"code"\s*:\s*"""".toRegex()
        val match = startMarker.find(json) ?: throw IllegalStateException("Missing 'code' field")
        val startIdx = match.range.last + 1

        // Find the closing quote (not escaped)
        var i = startIdx
        val sb = StringBuilder()
        while (i < json.length) {
            if (json[i] == '\\' && i + 1 < json.length) {
                when (json[i + 1]) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    else -> {
                        sb.append(json[i])
                        sb.append(json[i + 1])
                    }
                }
                i += 2
            } else if (json[i] == '"') {
                break
            } else {
                sb.append(json[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun pushToFirestore(testData: Map<String, String>) {
        val db = Firebase.firestore
        db.collection("generated_tests")
            .add(testData)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Test pushed to Firestore: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore push failed", e)
            }
    }

    private fun getScreenDensity(): Int {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.densityDpi
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Magic QA Capture",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Screen capture for automated test generation"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Magic QA")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
