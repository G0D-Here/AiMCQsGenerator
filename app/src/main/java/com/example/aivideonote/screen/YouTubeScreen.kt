package com.example.aivideonote.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(
    modifier: Modifier,
    viewModel: GeminiViewModel = hiltViewModel(),
    text: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller: LifecycleCameraController =
        remember { LifecycleCameraController(context) }
    var extractedText by remember { mutableStateOf("") }
    var granted by rememberSaveable { mutableStateOf(false) }


    CameraPermissionHandler {
        granted = true
    }
    if (granted)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { it ->
                    PreviewView(it).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }.also { previewView ->
                        startTextRecognition(
                            controller,
                            lifecycleOwner,
                            previewView, context, text = { extractedText = it }
                        )
                    }
                }, Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Box(
                Modifier
                    .align(Alignment.Center)
                    .border(1.dp, Color.Green)
            )
            Button(
                onClick = {
                    text(extractedText)
                    viewModel.camera = false
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(60.dp)
            ) {
                Text("Click")
            }
        }
}

@Composable
fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionRequested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            permissionRequested = true
        }
    }

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (permissionRequested) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Camera permission is needed to use this feature.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text("Open App Settings")
            }
        }
    }
}


private fun startTextRecognition(
    controller: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    previewView: PreviewView,
    context: Context,
    text: (String) -> Unit
) {
    controller.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    controller.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyser(text)
    )
    controller.bindToLifecycle(lifecycle)
    previewView.controller = controller
}

class TextRecognitionAnalyser(private val imageToText: (String) -> Unit = {}) :
    ImageAnalysis.Analyzer {
    companion object {
        const val THROTTLE_TIME_OUT = 1_000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val mediaImage = imageProxy.image ?: run { imageProxy.close();return@launch }
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            suspendCoroutine { continuation ->
                scanner.process(inputImage).addOnSuccessListener {
                    val detectedText = it.text
                    if (detectedText.isNotEmpty()) imageToText(detectedText)
                }.addOnCompleteListener {
                    continuation.resume(Unit)
                }
            }
            delay(THROTTLE_TIME_OUT)
        }.invokeOnCompletion {
            it?.printStackTrace()
            imageProxy.close()
        }

    }
}