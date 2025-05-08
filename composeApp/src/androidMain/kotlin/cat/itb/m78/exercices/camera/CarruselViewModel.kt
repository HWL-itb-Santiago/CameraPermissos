package cat.itb.m78.exercices.camera

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import cat.itb.m78.exercices.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarruselViewModel: ViewModel() {
    private val _uriImages = MutableStateFlow<List<String>>(emptyList())
    val uriImages: StateFlow<List<String>> = _uriImages.asStateFlow()

    fun getAllPhotos(): List<String> {
        try {
            val photos = database.markersQueries.selectAll().executeAsList()
            val photoUris = photos.map { it.markerData }
            _uriImages.value = photoUris
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error al recuperar fotos", e)
        }
        return _uriImages.value
    }
}