package cat.itb.m78.exercices.mapScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.m78.exercices.database
import cat.itb.m78.exercises.db.MarkersDB
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomMarker(val id: String, val position: LatLng, val uri : String? = null)

class MapScreenViewModel : ViewModel() {
    private val _markers = MutableStateFlow<List<MarkersDB>>(emptyList())
    val markers: StateFlow<List<MarkersDB>> = _markers.asStateFlow()

    private val _mapLoaded = MutableStateFlow(false)
    val mapLoaded: StateFlow<Boolean> = _mapLoaded.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            try {
                val markersList = database.markersQueries.selectAll().executeAsList()
                _markers.value = markersList
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    fun onMapLoaded() {
        _mapLoaded.value = true
    }

    fun refreshMarkers() {
        loadMarkers()
    }

    fun addMarker(marker: List<MarkersDB>) {
        _markers.value = _markers.value + marker
    }
    fun removeMarker(marker: MarkersDB) {
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
//                database.markersQueries.deleteAll()
            } catch (e: Exception) {
                Log.e("Database", "Error al eliminar todas las fotos", e)
            }
        }
    }
}