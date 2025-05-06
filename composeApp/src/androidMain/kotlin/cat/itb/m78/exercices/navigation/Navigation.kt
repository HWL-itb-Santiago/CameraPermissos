package cat.itb.m78.exercices.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cat.itb.m78.exercices.camera.Carrusel
import cat.itb.m78.exercices.camera.CurrentLocationContent
import cat.itb.m78.exercices.permissions.PermissionsScreen
import kotlinx.serialization.Serializable

data object Destination
{
    @Serializable
    data object PermissionsScreen

    @Serializable
    data object LocationScreen

    @Serializable
    data class  CarouselScreen(val uriImages: List<String>)
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destination.PermissionsScreen
    )
    {
        composable<Destination.PermissionsScreen> {
            PermissionsScreen()
        }

        composable<Destination.CarouselScreen> {backStack ->
            val listOfImages =backStack.toRoute<Destination.CarouselScreen>().uriImages
            Carrusel(listOfImages,
                goToCameraScreen = {
                    navController.navigate(Destination.LocationScreen)
                }
            )
        }
    }
}