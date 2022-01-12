package com.joeloewi.mediastoreimagegallery.ui.mediastoreimagegallery

import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.contentValuesOf
import androidx.navigation.NavController
import com.joeloewi.mediastoreimagegallery.R
import com.joeloewi.mediastoreimagegallery.ui.theme.MediaStoreImageGalleryTheme
import com.joeloewi.mediastoreimagegallery.viewmodel.CameraViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Composable
fun CameraScreen(
    navController: NavController,
    cameraViewModel: CameraViewModel
) {

    CameraContent(
        onShutterClick = {
            cameraViewModel.playShutterClickSound()
        }
    )
}

@Composable
fun CameraContent(
    onShutterClick: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val localLifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current
    val cameraController = remember { LifecycleCameraController(localContext) }
    val (isCapturing, onCaptureStateChange) = remember { mutableStateOf(false) }

    LaunchedEffect(cameraController) {
        cameraController.initializationFuture.runCatching {
            await()
        }.onSuccess {
            cameraController.apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
                isPinchToZoomEnabled = true
                isTapToFocusEnabled = true
                bindToLifecycle(localLifecycleOwner)
            }
        }.onFailure { cause ->
            cause.message?.let {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) {
        Box {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { androidViewContext ->
                    PreviewView(androidViewContext).apply {
                        controller = cameraController.apply {
                            scaleType = PreviewView.ScaleType.FIT_CENTER
                        }
                    }
                }
            )
            AnimatedVisibility(
                visible = isCapturing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.2f)
                    .background(
                        color = Color(0x80000000)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator()
                    } else {
                        FloatingActionButton(
                            backgroundColor = Color.White,
                            onClick = {
                                onShutterClick()
                                onCaptureStateChange(true)
                                val metadata = ImageCapture.Metadata()
                                val contentValues = contentValuesOf(
                                    MediaStore.MediaColumns.DISPLAY_NAME to "${
                                        localContext.getString(
                                            R.string.app_name
                                        )
                                    }_${System.currentTimeMillis()}",
                                    MediaStore.MediaColumns.MIME_TYPE to "image/jpeg"
                                )
                                val outputOptions = ImageCapture.OutputFileOptions
                                    .Builder(
                                        localContext.contentResolver,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        contentValues
                                    )
                                    .setMetadata(metadata)
                                    .build()

                                cameraController.takePicture(
                                    outputOptions,
                                    Dispatchers.IO.asExecutor(),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            outputFileResults.savedUri?.let { uri ->
                                                localContext.contentResolver.notifyChange(
                                                    uri,
                                                    null
                                                )
                                            }
                                            onCaptureStateChange(false)
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            onCaptureStateChange(false)
                                        }
                                    }
                                )
                            }
                        ) {

                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    MediaStoreImageGalleryTheme {
        CameraContent(
            onShutterClick = {

            }
        )
    }
}