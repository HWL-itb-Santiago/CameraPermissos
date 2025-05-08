package cat.itb.m78.exercices.permissions

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import cat.itb.m78.exercices.navigation.Menu

@SuppressLint("MissingPermission")
@Composable
fun PermissionsScreen(
    navController: NavHostController,
    // goToCameraScreen: () -> Unit,
    // goToLocationScreen: () -> Unit,
    // goToSettingsScreen: () -> Unit,
    // goToMainScreen: () -> Unit,
) {
    val permissions = listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.CAMERA,
    )
    PermissionBox(
        permissions = permissions,
        requiredPermissions = permissions,
        onGranted = {
            Menu(
                navHostController = navController,
                // goToCameraScreen = goToCameraScreen,
                // goToLocationScreen = goToLocationScreen,
                // goToSettingsScreen = goToSettingsScreen,
                // goToMainScreen = goToMainScreen,
            )
        },
    )
}