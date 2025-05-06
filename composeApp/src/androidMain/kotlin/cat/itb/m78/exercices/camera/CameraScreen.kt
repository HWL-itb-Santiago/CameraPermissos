package cat.itb.m78.exercices.camera

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.itb.m78.exercices.permissions.PermissionBox
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(goToCarrouselScreen: (List<String>) -> Unit){
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    if (!cameraPermissionState.status.isGranted)
    {
        RequiresPermissions()
    }
    else
    {
        val viewModel = viewModel { CameraViewModel() }
        val context = LocalContext.current
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

@Composable
fun MapsScreen(){
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 11f)
    }
    Box(Modifier.fillMaxSize()) {
        GoogleMap(cameraPositionState = cameraPositionState)
        Button(onClick = {
            // Move the camera to a new zoom level
            cameraPositionState.move(CameraUpdateFactory.zoomIn())
        }) {
            Text(text = "Zoom In")
        }
    }
}
@SuppressLint("UnrememberedMutableState")
@Composable
fun MapTarget(currentLat: Double, currentLng: Double) {
    GoogleMap(
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("DEMO_MAP_ID")
        },
    ) {
        AdvancedMarker(
            state = MarkerState(position = LatLng(currentLat, currentLng)),
            title = "My Position"
        )
    }
}
@RequiresPermission(
    anyOf = [android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun CurrentLocationContent(usePreciseLocation: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }
    val currentLat = remember { mutableStateOf(0.0) }
    val currentLng = remember { mutableStateOf(87.0) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(currentLat.value, currentLng.value) {
        val newPosition = LatLng(currentLat.value, currentLng.value)
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newPosition, 15f))
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(cameraPositionState = cameraPositionState)
        MapTarget(currentLat.value, currentLng.value)
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val result = locationClient.lastLocation.await()
                        locationInfo = if (result == null) {
                            "No last known location. Try fetching the current location first"
                        } else {
                            "Current location is \n" + "lat : ${result.latitude}\n" +
                                    "long : ${result.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                        }
                    }
                },
            ) {
                Text("Get last known location")
            }

            Button(
                onClick = {
                    //To get more accurate or fresher device location use this method
                    scope.launch(Dispatchers.IO) {
                        val priority = if (usePreciseLocation) {
                            Priority.PRIORITY_HIGH_ACCURACY
                        } else {
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY
                        }
                        val result = locationClient.getCurrentLocation(
                            priority,
                            CancellationTokenSource().token,
                        ).await()
                        result?.let { fetchedLocation ->
                            locationInfo =
                                "Current location is \n" + "lat : ${fetchedLocation.latitude}\n" +
                                        "long : ${fetchedLocation.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                        }
                        currentLat.value = result.latitude
                        currentLng.value = result.longitude
                        withContext(Dispatchers.Main) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(result.latitude, result.longitude),
                                    11f
                                )
                            )
                        }
                    }
                },
            ) {
                Text(text = "Get current location")
            }
            Text(
                text = locationInfo,
            )
        }
    }
}
