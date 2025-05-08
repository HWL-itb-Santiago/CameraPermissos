package cat.itb.m78.exercices.mapScreen

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
    val photoUri: String,
    val markerName: String,
    val markerData: String,
)

class MarkerDetailScreenViewModel: ViewModel() {
    private val _image = MutableStateFlow<Marker?>(null)
    val image: StateFlow<Marker?> = _image.asStateFlow()

    fun getMarkerDetails(photoUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val marker = database.markersQueries.selectById(photoUri).executeAsOneOrNull()
                if (marker != null) {
                    _image.value = Marker(
                        photoUri = marker.id,
                        markerName = marker.markerName,
                        markerData = marker.markerData,
                    )
                }
            } catch (e: Exception) {
                Log.e("Database", "Error al recuperar fotos", e)
            }
        }
    }
}
