package cat.itb.m78.exercices.camera

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import androidx.core.net.toUri

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun CameraScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: CameraViewModel = viewModel()

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLng by viewModel.currentLong.collectAsState()

    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.Main) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Se necesita permiso de cámara para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solicitar Permiso")
                }
            }
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

                // Overlay para mejorar la visibilidad de los controles
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Top Bar
                TopAppBar(
                    title = { Text("Cámara") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )

                // Location Info
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Lat: ${String.format("%.6f", currentLat)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Lng: ${String.format("%.6f", currentLng)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Bottom Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    // Camera Controls
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Gallery Button
                        FloatingActionButton(
                            onClick = {
                                val photos = viewModel.getAllPhotos()
                                navController.navigate("carouselScreen/${photos.joinToString(",")}")
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Galería")
                        }

                        // Capture Button
                        FloatingActionButton(
                            onClick = { viewModel.takePhoto(context) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Capturar",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Settings Button
                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    }
                }
            }
        }
    }
}


