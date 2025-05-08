package cat.itb.m78.exercices.navigation

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cat.itb.m78.exercices.camera.CameraScreen
import cat.itb.m78.exercices.camera.Carrusel
import cat.itb.m78.exercices.mapScreen.MapScreen
import cat.itb.m78.exercices.mapScreen.MarkerDetailScreen
import cat.itb.m78.exercices.permissions.PermissionsScreen
import kotlinx.serialization.Serializable

data object Destination
{
    @Serializable
    data object MapScreen

    @Serializable
    data object CameraScreen

    @Serializable
    data object Menu

    @Serializable
    data object PermissionsScreen

    @Serializable
    data object LocationScreen

    @Serializable
    data class CarouselScreen(val uriImages: List<String>)

    @Serializable
    data class MarkerDetailScreen(val photoUri: String)
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Destination.PermissionsScreen
    ) {
        composable<Destination.MapScreen> {
            MapScreen(true, navController)
        }
        composable<Destination.CameraScreen> {
            CameraScreen(navController)
        }
        composable<Destination.Menu>{
            Menu(navHostController = navController)
        }
        composable<Destination.PermissionsScreen> {
            PermissionsScreen(navController = navController)
        }
        
        composable<Destination.CarouselScreen> { backStackEntry ->
            val uriImages = backStackEntry.toRoute<Destination.CarouselScreen>().uriImages
            Carrusel(
                uriImages = uriImages,
                navController = navController
            )
        }

        composable<Destination.MarkerDetailScreen> { backStackEntry ->
            val photoUri = backStackEntry.toRoute<Destination.MarkerDetailScreen>().photoUri
            MarkerDetailScreen(
                photoUri = photoUri,
                navController = navController
            )
        }
    }
}