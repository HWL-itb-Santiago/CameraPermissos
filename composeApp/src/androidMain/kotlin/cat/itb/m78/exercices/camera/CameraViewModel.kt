package cat.itb.m78.exercices.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel() : ViewModel(){
    val listOfPhotos = mutableListOf<String>()
    var lastPhotoTaken = mutableStateOf("")

    private val _surferRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surferRequest: StateFlow<SurfaceRequest?> = _surferRequest.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surferRequest.value = newSurfaceRequest
        }
    }

    fun closeCamera()
    {
        _surferRequest.value = null
    }

    val imageCaptureUseCase: ImageCapture = ImageCapture.Builder().build()
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider.bindToLifecycle(lifecycleOwner, DEFAULT_BACK_CAMERA, cameraPreviewUseCase, imageCaptureUseCase
        )
        try { awaitCancellation() } finally { processCameraProvider.unbindAll() }
    }

    fun takePhoto(context: Context) {
        val name = "photo_"+ System.nanoTime()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        imageCaptureUseCase.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraPreview", "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraPreview", "Photo capture succeeded: ${output.savedUri}")
                    lastPhotoTaken.value = output.savedUri?.toString() ?: ""
                    listOfPhotos.add(lastPhotoTaken.value)
                }
            }
        )
    }
    fun showPhoto()
    {
//        ImageCapture.OnImageCapturedCallback
    }
}