package cat.itb.m78.exercices.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(goToCarrouselScreen: (List<String>) -> Unit){
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    if (!cameraPermissionState.status.isGranted)
    {
        FeatureThatRequiresCameraPermission()
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
