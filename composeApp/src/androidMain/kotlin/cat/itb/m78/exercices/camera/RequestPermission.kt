package cat.itb.m78.exercices.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(
    permissionState: PermissionState,
    rationaleText: String,
    deniedText: String,
    onRequest: () -> Unit
) {
    val textToShow = if (permissionState.status.shouldShowRationale) rationaleText else deniedText
    Column {
        Text(textToShow)
        Button(onClick = onRequest) {
            Text("Request permission")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequiresPermissions() {
    val cameraPermission = rememberCameraPermissionState()
    val locationPermission = rememberLocationPermissionState()

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            cameraPermission.status.isGranted -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission granted")

                    if (locationPermission.status.isGranted) {
                        Text("Maps permission granted")
                    } else {
                        PermissionRationale(
                            permissionState = locationPermission,
                            rationaleText = "Maps is important for this app. Please grant the permission.",
                            deniedText = "Maps permission required for this feature.",
                            onRequest = { locationPermission.launchPermissionRequest() }
                        )
                    }
                }
            }

            else -> {
                PermissionRationale(
                    permissionState = cameraPermission,
                    rationaleText = "The camera is important for this app. Please grant the permission.",
                    deniedText = "Camera permission required for this feature.",
                    onRequest = { cameraPermission.launchPermissionRequest() }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraPermissionState() = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionState() = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)