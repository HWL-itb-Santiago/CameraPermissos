package cat.itb.m78.exercices.mapScreen

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.Bitmap
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import cat.itb.m78.exercices.database
import cat.itb.m78.exercices.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun MapScreen(usePreciseLocation: Boolean, navController: NavController) {
    val mapViewModel: MapScreenViewModel = viewModel()
    val markers by mapViewModel.markers.collectAsState()
    val mapLoaded by mapViewModel.mapLoaded.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var locationInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val currentLat = remember { mutableStateOf(0.0) }
    val currentLng = remember { mutableStateOf(0.0) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    var showRegistrationSheet by remember { mutableStateOf(false) }
    var flowerSpecies by remember { mutableStateOf("") }
    var flowerColor by remember { mutableStateOf("") }
    var flowerNotes by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scopeDrawer = rememberCoroutineScope()

    // Función que abre el Drawer
    fun openDrawer() {
        scopeDrawer.launch { drawerState.open() }
    }

    // Se lanza al cargar el mapa
    LaunchedEffect(mapLoaded) {
        try {
            val result = withContext(Dispatchers.Main) {
                locationClient.getCurrentLocation(
                    if (usePreciseLocation) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            }

            if (result != null) {
                val latLng = LatLng(result.latitude, result.longitude)
                currentLat.value = result.latitude
                currentLng.value = result.longitude
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                )
            } else {
                locationInfo = "No se pudo obtener la ubicación actual."
            }
        } catch (e: Exception) {
            locationInfo = "Error al obtener la ubicación: ${e.localizedMessage}"
            Log.e("Location", "Error", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Fotos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Destination.CameraScreen) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Tomar Foto")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Indicador de carga mientras se obtiene ubicación
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Mapa y elementos después de obtener ubicación
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    onMapLoaded = { mapViewModel.onMapLoaded() }
                ) {
                    // Mostrar marcadores de fotos
                    markers.forEach { marker ->
                        Marker(
                            state = MarkerState(position = LatLng(marker.markerLat, marker.markerLong)),
                            title = marker.markerName,
                            snippet = "Ver foto",
                            onClick = {
                                navController.navigate("markerDetailScreen/${marker.markerData}")
                                true
                            }
                        )
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                }
                // FAB para añadir flor
//                FloatingActionButton(
//                    onClick = {
//                        // Aquí capturas o seleccionas la foto
//                        showRegistrationSheet = true
//                    },
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(16.dp),
//                    containerColor = MaterialTheme.colorScheme.primary
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = "Registrar flor")
//                }

                // Modal de registro
                // No implementado aun
//                if (showRegistrationSheet) {
//                    ModalBottomSheet(
//                        onDismissRequest = { showRegistrationSheet = false }
//                    ) {
//                        Column(Modifier.padding(16.dp)) {
//                            OutlinedTextField(
//                                value = flowerSpecies,
//                                onValueChange = { flowerSpecies = it },
//                                label = { Text("Especie") }
//                            )
//
//                            OutlinedTextField(
//                                value = flowerColor,
//                                onValueChange = { flowerColor = it },
//                                label = { Text("Color") }
//                            )
//
//                            OutlinedTextField(
//                                value = flowerNotes,
//                                onValueChange = { flowerNotes = it },
//                                label = { Text("Notas") },
//                                maxLines = 4
//                            )
//
//                            Button(
//                                onClick = {
//                                    // Guardar flor con lat/lng actuales
//                                    showRegistrationSheet = false
//                                },
//                                modifier = Modifier.align(Alignment.End)
//                            ) {
//                                Text("Guardar")
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}



