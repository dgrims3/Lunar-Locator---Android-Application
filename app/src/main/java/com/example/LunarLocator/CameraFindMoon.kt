package com.example.LunarLocator


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.LunarLocator.models.OrientationSensorManager
import com.example.LunarLocator.ui.theme.CameraIconWhite
import com.google.common.util.concurrent.ListenableFuture

class CameraFindMoon : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moonAzimuthAndAltitude = intent.getDoubleArrayExtra("azimuth_and_altitude")
        val moonAzimuth: Float? = moonAzimuthAndAltitude?.get(0)?.toFloat()
        val moonAltitude: Float? = moonAzimuthAndAltitude?.get(1)?.toFloat()

        setContent {
            val context = LocalContext.current
            val orientationManager = remember { OrientationSensorManager(context) }

            DisposableEffect(Unit) {
                orientationManager.start()
                onDispose {
                    orientationManager.stop()
                }
            }
            val azimuth = orientationManager.azimuth.value
            val pitch = orientationManager.pitch.value

            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview() // Your camera preview
                // OverlayBoxAndArrows(CameraIconWhite,CameraIconWhite, CameraIconWhite, CameraIconWhite)
                Box(
                    modifier = Modifier
                        .size(200.dp) // You can adjust the size
                        .align(Alignment.Center)
                        .border(width = 2.dp, color = CameraIconWhite, shape = CircleShape)
                )

                // Display sensor values
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(text = "Azimuth: %.2f".format(azimuth), color = CameraIconWhite)
                    Text(text = "Altitude: %.2f".format(pitch), color = CameraIconWhite)
                }
                // Display current moon location
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(text = "Moon Azimuth: %.2f".format(moonAzimuth), color = CameraIconWhite)
                    Text(text = "Moon Altitude: %.2f".format(moonAltitude), color = CameraIconWhite)
                }
            }
        }
    }
}

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview
                        )

                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun OverlayBoxAndArrows(rightArrowColor: Color, leftArrowColor: Color, upArrowColor: Color, downArrowColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.baseline_keyboard_arrow_right_24),
            contentDescription = "Custom Right Arrow",
            colorFilter = ColorFilter.tint(rightArrowColor),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp)
                .width(100.dp)
                .height(200.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.baseline_keyboard_arrow_left_24),
            contentDescription = "Custom Left Arrow",
            colorFilter = ColorFilter.tint(leftArrowColor),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 0.dp)
                .width(100.dp)
                .height(200.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.baseline_keyboard_arrow_up_24),
            contentDescription = "Custom Up Arrow",
            colorFilter = ColorFilter.tint(upArrowColor),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(end = 0.dp)
                .width(100.dp)
                .height(200.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24),
            contentDescription = "Custom Down Arrow",
            colorFilter = ColorFilter.tint(downArrowColor),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(end = 0.dp)
                .width(100.dp)
                .height(200.dp)
        )
    }
}
