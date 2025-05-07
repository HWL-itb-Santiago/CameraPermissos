package cat.itb.m78.exercices.permissions

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.core.net.toUri

/**
 * The PermissionBox uses a [Box] to show a simple permission request UI when the provided [permission]
 * is revoked or the provided [onGranted] content if the permission is granted.
 *
 * This composable follows the permission request flow but for a complete example check the samples
 * under privacy/permissions
 */
@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permission: String,
    description: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable BoxScope.() -> Unit,
) {
    PermissionBox(
        modifier,
        permissions = listOf(permission),
        requiredPermissions = listOf(permission),
        description,
        contentAlignment,
    ) { onGranted() }
}

/**
 * A variation of [PermissionBox] that takes a list of permissions and only calls [onGranted] when
 * all the [requiredPermissions] are granted.
 *
 * By default it assumes that all [permissions] are required.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permissions: List<String>,
    requiredPermissions: List<String> = permissions,
    description: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable BoxScope.(List<String>) -> Unit,
) {
    val context = LocalContext.current
    var errorText by remember {
        mutableStateOf("")
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions) { map ->
        val rejectedPermissions = map.filterValues { !it }.keys
        errorText = if (rejectedPermissions.none { it in requiredPermissions }) {
            ""
        } else {
            "${rejectedPermissions.joinToString()} required for the sample"
        }
    }
    val allRequiredPermissionsGranted =
        permissionState.revokedPermissions.none { it.permission in requiredPermissions }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = if (allRequiredPermissionsGranted) {
            contentAlignment
        } else {
            Alignment.Center
        },
    ) {
        if (allRequiredPermissionsGranted) {
            onGranted(
                permissionState.permissions
                    .filter { it.status.isGranted }
                    .map { it.permission },
            )
        } else {
            PermissionScreen(
                permissionState,
                description,
                errorText,
            )

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:${context.packageName}".toUri(),
                    )
                    context.startActivity(intent)
                },
            ) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "App settings")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionScreen(
    state: MultiplePermissionsState,
    description: String?,
    errorText: String,
) {
    var showRationale by remember(state) {
        mutableStateOf(false)
    }

    val permissions = remember(state.revokedPermissions) {
        state.revokedPermissions.joinToString("\n") {
            "• " + it.permission.removePrefix("android.permission.")
        }
    }

    androidx.compose.material3.Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(32.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permisos requeridos",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Acceso a la cámara y a la ubicación",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    if (state.shouldShowRationale) {
                        showRationale = true
                    } else {
                        state.launchMultiplePermissionRequest()
                    }
                }
            ) {
                Text(text = "Conceder permisos")
            }
            if (errorText.isNotBlank()) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permisos necesarios") },
            text = {
                Text("La aplicación necesita los siguientes permisos:\n\n$permissions")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    state.launchMultiplePermissionRequest()
                }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
