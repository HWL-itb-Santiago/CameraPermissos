package cat.itb.m78.exercices.permissions

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import cat.itb.m78.exercices.camera.CurrentLocationContent

@SuppressLint("MissingPermission")
@Composable
fun PermissionsScreen(
) {
    val permissions = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.CAMERA,
    )
    PermissionBox(
        permissions = permissions,
        requiredPermissions = permissions,
        onGranted = {
            CurrentLocationContent(
                usePreciseLocation = it.contains(android.Manifest.permission.ACCESS_FINE_LOCATION),
            )
        },
    )
}