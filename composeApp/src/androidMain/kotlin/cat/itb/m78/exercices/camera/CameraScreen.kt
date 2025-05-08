package cat.itb.m78.exercices.camera

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalPermissionsApi::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun CameraScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel : CameraViewModel = viewModel()

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLng by viewModel.currentLong.collectAsState()

    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.Main) {
                // Espera a que el cliente de ubicación esté disponible
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            }
            if (result != null) {
                viewModel.updateLocation(result.latitude, result.longitude)
            }
        } catch (e: Exception) {
            Log.e("Location", "Error", e)
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        // Muestra algo mientras no se conceda el permiso
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de cámara para continuar.")
        }
    } else {
        LaunchedEffect(Unit) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }


        val surfaceRequest by viewModel.surferRequest.collectAsState()

        surfaceRequest?.let { request ->
            Box(modifier = Modifier.fillMaxSize()) {
                CameraXViewfinder(
                    surfaceRequest = request,
                    modifier = Modifier.fillMaxSize()
                )

                // Botón para cerrar cámara
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp, top = 50.dp)
                ) {
                    Button(onClick = { viewModel.closeCamera() }) {
                        Text("Cerrar cámara")
                    }
                }

                // Botones inferiores
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 150.dp)
                ) {
                    Row {
                        Button(onClick = { viewModel.takePhoto(context) }) {
                            Text("📸 Capturar")
                        }

                        Button(onClick = {
                            // Navegar a la galería/carrousel si quieres:
                            // navController.navigate("carrousel")
                        }) {
                            Text("🖼 Ver galería")
                        }
                        // Display the location
                        Text(
                            text = "Lat: ${currentLat}, Lng: ${currentLng}",
                            modifier = Modifier
                                .padding(16.dp)
                                .animateContentSize()
                        )
                    }
                }
            }
        }
    }
}


