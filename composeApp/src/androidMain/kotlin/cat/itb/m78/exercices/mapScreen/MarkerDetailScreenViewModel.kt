package cat.itb.m78.exercices.mapScreen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Marker(
    val id: String,
    val photoUri: String,
    val markerName: String,
    val markerData: String,
    val markerLat: Double,
    val markerLong: Double
)

class MarkerDetailScreenViewModel : ViewModel() {
    private val _image = MutableStateFlow<Marker?>(null)
    val image: StateFlow<Marker?> = _image.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getMarkerDetails(photoUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("MarkerDetail", "Buscando foto con URI/ID: $photoUri")
                
                // Intentar parsear el URI para verificar si es válido
                val uri = try {
                    Uri.parse(photoUri)
                } catch (e: Exception) {
                    Log.e("MarkerDetail", "URI inválido: $photoUri", e)
                    null
                }

                // Buscar el marcador
                val marker = if (uri != null) {
                    // Si es un URI válido, buscar por el URI
                    Log.d("MarkerDetail", "Buscando por URI: ${uri.toString()}")
                    database.markersQueries.selectByUri(uri.toString()).executeAsOneOrNull()
                } else {
                    // Si no es un URI válido, buscar por ID
                    Log.d("MarkerDetail", "Buscando por ID: $photoUri")
                    database.markersQueries.selectById(photoUri).executeAsOneOrNull()
                }

                if (marker != null) {
                    Log.d("MarkerDetail", "Foto encontrada: ${marker.markerName}")
                    _image.value = Marker(
                        id = marker.id,
                        photoUri = marker.markerData,
                        markerName = marker.markerName,
                        markerData = marker.markerData,
                        markerLat = marker.markerLat,
                        markerLong = marker.markerLong
                    )
                } else {
                    Log.e("MarkerDetail", "No se encontró la foto con URI/ID: $photoUri")
                    _error.value = "No se encontró la foto"
                }
            } catch (e: Exception) {
                Log.e("MarkerDetail", "Error al recuperar foto", e)
                _error.value = "Error al cargar la foto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMarker(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("MarkerDetail", "Eliminando foto con ID: $id")
                database.markersQueries.deleteById(id)
                _image.value = null
            } catch (e: Exception) {
                Log.e("MarkerDetail", "Error al eliminar foto", e)
                _error.value = "Error al eliminar la foto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMarkerName(id: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("MarkerDetail", "Actualizando nombre de foto: $id -> $newName")
                database.markersQueries.updateMarkerName(newName, id)
                // Actualizar el estado local
                _image.value?.let { currentMarker ->
                    _image.value = currentMarker.copy(markerName = newName)
                }
            } catch (e: Exception) {
                Log.e("MarkerDetail", "Error al actualizar nombre", e)
                _error.value = "Error al actualizar el nombre: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshMarker(id: String) {
        getMarkerDetails(id)
    }

    fun clearError() {
        _error.value = null
    }
}
