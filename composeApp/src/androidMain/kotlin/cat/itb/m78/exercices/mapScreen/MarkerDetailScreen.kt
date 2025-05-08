package cat.itb.m78.exercices.mapScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.itb.m78.exercices.database
import coil3.Image
import coil3.compose.AsyncImage

@Composable
fun MarkerDetailScreen(photoUri: String?) {
    val viewModel: MarkerDetailScreenViewModel = viewModel()
    val marker = viewModel.image.collectAsState().value

    LaunchedEffect(Unit) {
        // Asegúrate de que el URI no sea nulo antes de realizar la consulta
        if (photoUri != null) {
            viewModel.getMarkerDetails(photoUri)
        }
    }

    if (photoUri != null) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Verificamos si la consulta fue exitosa
            marker?.let {
                // Mostrar la imagen utilizando el URI de la base de datos
                AsyncImage(
                    model = it.photoUri, // Suponiendo que 'photoUri' es la URL de la imagen
                    contentDescription = "Imagen de la flor",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar los datos asociados a la imagen
                Text("Nombre del marcador: ${it.markerName}")
                Text("Descripción: ${it.markerData}")
            } ?: run {
                // Mostrar un mensaje en caso de que no se encuentre la imagen
                Text("No se encontró información para este marcador.")
            }
        }
    } else {
        Text("No se proporcionó un URI de la foto.")
    }
}
