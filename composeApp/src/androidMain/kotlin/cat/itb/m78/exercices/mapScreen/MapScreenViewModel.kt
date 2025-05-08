package cat.itb.m78.exercices.mapScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.database
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomMarker(val id: String, val position: LatLng, val uri : String? = null)

class MapScreenViewModel: ViewModel() {

    private val _markers = MutableStateFlow<List<CustomMarker>>(emptyList())
    val markers : StateFlow<List<CustomMarker>> = _markers.asStateFlow()

    fun addMarker(marker: List<CustomMarker>) {
        _markers.value = _markers.value + marker
    }
    fun removeMarker(marker: CustomMarker) {
        _markers.value = _markers.value - marker
    }

    fun deleteMarker(idPhoto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.markersQueries.deleteById(idPhoto)
            } catch (e: Exception) {
                Log.e("Database", "Error al eliminar foto", e)
            }
        }
    }
    fun deleteAllMarkers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.markersQueries.deleteAll()
            } catch (e: Exception) {
                Log.e("Database", "Error al eliminar todas las fotos", e)
            }
        }
    }
    fun getMarkers() {
        // Ejecuta la consulta y obtén las fotos y las ubicaciones
        val photoData = database.markersQueries.selectAll().executeAsList()

        // Mapea los resultados de la consulta a una lista de CustomMarker
        addMarker(
            photoData.map { photo ->
                CustomMarker(
                    id = photo.id,
                    position = LatLng(photo.markerLat, photo.markerLong),
                    uri = photo.markerData
                )
            }
        )
    }

    private val _mapLoaded = MutableStateFlow(false)
    val mapLoaded: StateFlow<Boolean> = _mapLoaded.asStateFlow()

    fun setMapLoaded(loaded: Boolean) {
        _mapLoaded.value = loaded
    }
}