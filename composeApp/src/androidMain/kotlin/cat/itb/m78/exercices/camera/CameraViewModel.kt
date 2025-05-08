package cat.itb.m78.exercices.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
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
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.database
import cat.itb.m78.exercises.db.MarkersDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CameraViewModel() : ViewModel() {
    private val _locationClient = MutableStateFlow<android.location.LocationManager?>(null)
    var locationClient: StateFlow<android.location.LocationManager?> = _locationClient.asStateFlow()

    private val _currentLat = MutableStateFlow<Double>(0.0)
    var currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLong = MutableStateFlow<Double>(0.0)
    var currentLong: StateFlow<Double> = _currentLong.asStateFlow()

    private val _surferRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surferRequest: StateFlow<SurfaceRequest?> = _surferRequest.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surferRequest.value = newSurfaceRequest
        }
    }

    private val _currentPhotos = MutableStateFlow<List<String>>(emptyList())
    val currentPhotos: StateFlow<List<String>> = _currentPhotos.asStateFlow()

    fun updateLocation(lat: Double, lng: Double) {
        _currentLat.value = lat
        _currentLong.value = lng
    }

    fun setLocationClient(locationClient: android.location.LocationManager) {
        _locationClient.value = locationClient
    }

    fun getLocationClient(): android.location.LocationManager? {
        return _locationClient.value
    }

    fun closeCamera() {
        _surferRequest.value = null
    }

    fun insertPhoto(
        idPhoto: String,
        markerName: String,
        markerData: String,
        markerLat: Double,
        markerLong: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CameraViewModel", "Insertando foto: $markerName, URI: $markerData")
                database.markersQueries.insert(
                    id = idPhoto,
                    markerName = markerName,
                    markerData = markerData,
                    markerLat = markerLat,
                    markerLong = markerLong
                )
                Log.d("CameraViewModel", "Foto insertada correctamente")
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error al insertar foto", e)
            }
        }
    }

    fun deletePhoto(idPhoto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CameraViewModel", "Eliminando foto con ID: $idPhoto")
                database.markersQueries.deleteById(idPhoto)
                Log.d("CameraViewModel", "Foto eliminada correctamente")
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error al eliminar foto", e)
            }
        }
    }

    fun getAllPhotos(): List<String> {
        try {
            val photos = database.markersQueries.selectAll().executeAsList()
            val photoUris = photos.map { it.markerData }
            _currentPhotos.value = photoUris
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error al recuperar fotos", e)
        }
        return _currentPhotos.value
    }

    val imageCaptureUseCase: ImageCapture = ImageCapture.Builder().build()

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            DEFAULT_BACK_CAMERA,
            cameraPreviewUseCase,
            imageCaptureUseCase
        )
        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }

    fun takePhoto(context: Context) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
            .format(System.currentTimeMillis())
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
                    Log.e("CameraViewModel", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraViewModel", "Photo capture succeeded: ${output.savedUri}")
                    output.savedUri?.let { uri ->
                        try {
                            // Verificar que el URI sea válido
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                // Si podemos abrir el stream, el URI es válido
                                val photoId = generateUniqueIdPhoto()
                                val uriString = uri.toString()
                                Log.d("CameraViewModel", "URI válido, guardando foto con ID: $photoId")
                                insertPhoto(
                                    idPhoto = photoId,
                                    markerName = "Foto $name",
                                    markerData = uriString,
                                    markerLat = currentLat.value,
                                    markerLong = currentLong.value
                                )
                            } ?: run {
                                Log.e("CameraViewModel", "No se pudo abrir el stream del URI: $uri")
                            }
                        } catch (e: Exception) {
                            Log.e("CameraViewModel", "Error al procesar el URI: $uri", e)
                        }
                    }
                }
            }
        )
    }

    private fun generateUniqueIdPhoto(): String {
        return "photo_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}