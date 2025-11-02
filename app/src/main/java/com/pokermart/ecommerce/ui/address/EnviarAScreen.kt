package com.pokermart.ecommerce.ui.address

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pokermart.ecommerce.ui.common.EstadoVacio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarAScreen(
    viewModel: EnviarAViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                scope.launch {
                    manejarUbicacionActual(fusedClient, geocoder, viewModel)
                }
            } else {
                viewModel.mostrarMensaje("Necesitamos tu ubicacion para completar la direccion.")
            }
        }

    LaunchedEffect(state.mensaje) {
        val mensaje = state.mensaje ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensaje()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar a") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (state.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.direcciones.isEmpty()) {
                    EstadoVacio(
                        mensaje = "Aun no tienes direcciones guardadas.",
                        accionTexto = "Agregar direccion",
                        enAccionClick = { viewModel.abrirDialogoNuevaDireccion() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.direcciones) { direccion ->
                            DireccionItem(
                                item = direccion,
                                onSelect = { viewModel.seleccionarPredeterminada(direccion.id) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.abrirDialogoNuevaDireccion() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar direccion")
                    }
                    OutlinedButton(
                        onClick = {
                            if (tienePermisoUbicacion(context)) {
                                scope.launch {
                                    manejarUbicacionActual(fusedClient, geocoder, viewModel)
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Usar mi ubicacion")
                    }
                    Button(
                        onClick = {
                            if (state.direcciones.any { it.esPredeterminada }) {
                                onConfirm()
                            } else {
                                viewModel.mostrarMensaje("Selecciona una direccion predeterminada.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }

    if (state.mostrarDialogo) {
        DireccionDialog(
            state = state,
            onEtiquetaChange = viewModel::actualizarEtiqueta,
            onDireccionChange = viewModel::actualizarDireccion,
            onReferenciaChange = viewModel::actualizarReferencia,
            onPredeterminadaChange = viewModel::actualizarPredeterminada,
            onDismiss = viewModel::cerrarDialogo,
            onSave = viewModel::guardarDireccion
        )
    }
}

@Composable
private fun DireccionItem(
    item: DireccionItemUi,
    onSelect: () -> Unit
) {
    val etiqueta = item.etiqueta?.takeIf { it.isNotBlank() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = item.esPredeterminada,
                onClick = onSelect
            )
            Column(modifier = Modifier.weight(1f)) {
                if (etiqueta != null) {
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = item.direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.referencia?.let {
                    Text(
                        text = "Referencia: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (item.esPredeterminada) {
                    Text(
                        text = "Predeterminada",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DireccionDialog(
    state: EnviarAUiState,
    onEtiquetaChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onReferenciaChange: (String) -> Unit,
    onPredeterminadaChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onSave, enabled = !state.guardando) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.guardando) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                text = if (state.formulario.id == null) "Agregar direccion" else "Editar direccion",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.formulario.etiqueta,
                    onValueChange = onEtiquetaChange,
                    label = { Text("Etiqueta (ej: Casa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.formulario.direccion,
                    onValueChange = onDireccionChange,
                    label = { Text("Direccion") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.formulario.errorDireccion != null,
                    supportingText = {
                        state.formulario.errorDireccion?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                OutlinedTextField(
                    value = state.formulario.referencia,
                    onValueChange = onReferenciaChange,
                    label = { Text("Referencia (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.formulario.marcarComoPredeterminada,
                        onCheckedChange = { onPredeterminadaChange(it) }
                    )
                    Text("Marcar como predeterminada")
                }
            }
        }
    )
}

private fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

private suspend fun manejarUbicacionActual(
    fusedClient: FusedLocationProviderClient,
    geocoder: Geocoder,
    viewModel: EnviarAViewModel
) {
    val location = fusedClient.awaitCurrentLocation()
    if (location == null) {
        viewModel.mostrarMensaje("No pudimos obtener tu ubicacion.")
        return
    }
    val direccion = geocoder.obtenerDireccion(location.latitude, location.longitude)
    if (direccion == null) {
        viewModel.mostrarMensaje("No pudimos obtener la direccion para esta ubicacion.")
        return
    }
    viewModel.prepararFormularioConUbicacion(
        direccion = direccion,
        latitud = location.latitude,
        longitud = location.longitude
    )
}

private suspend fun FusedLocationProviderClient.awaitCurrentLocation(): Location? =
    suspendCancellableCoroutine { continuation ->
        val tokenSource = CancellationTokenSource()
        val task = getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
        task.addOnSuccessListener { location ->
            if (continuation.isActive) continuation.resume(location)
        }
        task.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
        continuation.invokeOnCancellation { tokenSource.cancel() }
    }

private suspend fun Geocoder.obtenerDireccion(latitud: Double, longitud: Double): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            getFromLocation(latitud, longitud, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val linea = addresses.firstOrNull()?.getAddressLine(0)
                    if (continuation.isActive) continuation.resume(linea)
                }

                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) continuation.resume(null)
                }
            })
        }
    } else {
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                getFromLocation(latitud, longitud, 1)
            }.getOrNull()
                ?.firstOrNull()
                ?.getAddressLine(0)
        }
    }
}
