package cat.itb.m78.exercices.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.material3.carousel.rememberCarouselState as rememberCarouselState1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carrusel(uirImages: List<String>, goToCameraScreen: () -> Unit)
{
    val listOfImages = uirImages
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    )
    {
        CarouselHorizontalExample(listOfImages)
        // Uncomment the following lines to display images in a vertical list
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(color = Color.White)
//        )
//        {
//            items(listOfImages) { image ->
//                AsyncImage(
//                    model = image,
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//        }
        Button(
            onClick = { goToCameraScreen() },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Back to Camera")
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun CarouselHorizontalExample(listOfImages: List<String>) {
    val state = rememberCarouselState1(itemCount = { listOfImages.size}, initialItem = 0)

    Column(verticalArrangement = Arrangement.Center) {
        HorizontalMultiBrowseCarousel(
            state = state,
            preferredItemWidth = 250.dp,
            modifier = Modifier.height(200.dp),
            itemSpacing = 10.dp
        ) { page ->
            Box(
                modifier =
                    Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .aspectRatio(0.5f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = listOfImages[page],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .background(Color.Gray)
                        .aspectRatio(0.5f),
                    contentScale = ContentScale.Crop
                )
                Text(text = page.toString(), fontSize = 32.sp, color = Color.White)
            }
        }
    }
}