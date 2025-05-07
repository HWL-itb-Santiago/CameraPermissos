package cat.itb.m78.exercices.camera

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.itb.m78.exercices.navigation.DrawerMenu
import cat.itb.m78.exercices.permissions.PermissionBox
import coil3.Bitmap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext as LocalContext1

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(goToCarrouselScreen: (List<String>) -> Unit){
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    if (!cameraPermissionState.status.isGranted)
    {
        RequiresPermissions()
    }
    else
    {
        val viewModel = viewModel { CameraViewModel() }
        val context = LocalContext1.current
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }
        val surfaceRequest by viewModel.surferRequest.collectAsState()
        surfaceRequest?.let { request ->
            Box {
                CameraXViewfinder(
                    surfaceRequest = request,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp, top = 50.dp),
                )
                {
                    Button({ viewModel.closeCamera() }) {
                        Text("Close Camera")
                    }
                }
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 150.dp),
                )
                {
                    Row {
                        Button({
                            viewModel.takePhoto(context)
                        }) {
                            Text("Take Photo")
                        }
                        Button({
                            goToCarrouselScreen(viewModel.listOfPhotos.toList())
                        }) {
                            Text("Carousel")
                        }
                    }
                }

            }
        }
    }
}

//@Composable
//fun MapsScreen(){
//    val singapore = LatLng(1.35, 103.87)
//    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(singapore, 11f)
//    }
//    Box(Modifier.fillMaxSize()) {
//        GoogleMap(cameraPositionState = cameraPositionState)
//        Button(onClick = {
//            // Move the camera to a new zoom level
//            cameraPositionState.move(CameraUpdateFactory.zoomIn())
//        }) {
//            Text(text = "Zoom In")
//        }
//    }
//}


data class CustomMarker(val id: String, val position: LatLng)

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapTarget(currentLat: Double, currentLng: Double) {
        AdvancedMarker(
            state = MarkerState(position = LatLng(currentLat, currentLng)),
            title = "My Position"
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun MapScreen(usePreciseLocation: Boolean) {
    val markers = remember { mutableStateListOf<CustomMarker>() }
    var mapLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext1.current
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
    var flowerPhoto by remember { mutableStateOf<Bitmap?>(null) }
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

                markers.add(CustomMarker("Mi ubicación", latLng))

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                )

                locationInfo =
                    "Ubicación actual:\nlat: ${result.latitude}, long: ${result.longitude}"
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

    // Aquí se invoca el DrawerMenu, pasando el contenido del mapa como argumento
    DrawerMenu(
//        drawerState = drawerState,
//        drawerContent = {
//            // Aquí defines el contenido de tu Drawer, como botones o listas
//            Column {
//                Text("Opción 1")
//                Text("Opción 2")
//                Button(onClick = { scopeDrawer.launch { drawerState.close() } }) {
//                    Text("Cerrar")
//                }
//            }
//        },
//        gesturesEnabled = drawerState.isOpen, // Solo habilitar gestos si el Drawer está abierto
        content = {
            Box(Modifier.fillMaxSize()) {
                // Aquí, agregar el botón para abrir el Drawer (por ejemplo, un icono de hamburguesa)
                IconButton(
                    onClick = { openDrawer() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                }

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
                        cameraPositionState = cameraPositionState,
                        googleMapOptionsFactory = { GoogleMapOptions().mapId("DEMO_MAP_ID") },
                        onMapClick = { latLng -> markers.add(CustomMarker("Marker ${markers.size + 1}", latLng)) },
                        onMapLoaded = {
                            mapLoaded = true
                        }
                    ) {
                        markers.forEach { marker ->
                            AdvancedMarker(
                                state = MarkerState(position = marker.position),
                                title = marker.id
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
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    val result = withContext(Dispatchers.IO) {
                                        locationClient.getCurrentLocation(
                                            Priority.PRIORITY_HIGH_ACCURACY,
                                            CancellationTokenSource().token
                                        ).await()
                                    }

                                    result?.let {
                                        val latLng = LatLng(it.latitude, it.longitude)
                                        currentLat.value = it.latitude
                                        currentLng.value = it.longitude
                                        markers.add(CustomMarker("Mi ubicación", latLng))

                                        withContext(Dispatchers.Main) {
                                            cameraPositionState.move(
                                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                            )
                                            locationInfo =
                                                "Ubicación actual:\nlat: ${it.latitude}, long: ${it.longitude}"
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                        ) {
                            Text("Actualizar ubicación")
                        }

                        Text(text = locationInfo)
                    }
                    // FAB para añadir flor
                    FloatingActionButton(
                        onClick = {
                            // Aquí capturas o seleccionas la foto
                            showRegistrationSheet = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Registrar flor")
                    }

                    // Modal de registro
                    if (showRegistrationSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showRegistrationSheet = false }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                flowerPhoto?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Foto de flor"
                                    )
                                } ?: Text("No se ha tomado foto aún.")

                                OutlinedTextField(
                                    value = flowerSpecies,
                                    onValueChange = { flowerSpecies = it },
                                    label = { Text("Especie") }
                                )

                                OutlinedTextField(
                                    value = flowerColor,
                                    onValueChange = { flowerColor = it },
                                    label = { Text("Color") }
                                )

                                OutlinedTextField(
                                    value = flowerNotes,
                                    onValueChange = { flowerNotes = it },
                                    label = { Text("Notas") },
                                    maxLines = 4
                                )

                                Button(
                                    onClick = {
                                        // Guardar flor con lat/lng actuales
                                        showRegistrationSheet = false
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}



