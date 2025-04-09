package cat.itb.m78.exercices.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage

@Composable
fun Carrusel(uirImages: List<String>)
{
    val listOfImages = uirImages
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    )
    {
        listOfImages.forEach()
        {image ->
            AsyncImage(
                model = image,
                contentDescription = "imagen"
            )
        }
        Text("Carrusel de fotos")
    }
}