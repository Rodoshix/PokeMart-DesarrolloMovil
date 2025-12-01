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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.pokermart.ecommerce.ui.common.EstadoVacio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.util.Locale

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
    var mostrarSelectorMapa by remember { mutableStateOf(false) }
    var procesandoSeleccionMapa by remember { mutableStateOf(false) }

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
                val direccionMapa = state.direcciones
                    .firstOrNull { it.esPredeterminada && it.latitud != null && it.longitud != null }
                    ?: state.direcciones.firstOrNull { it.latitud != null && it.longitud != null }

                direccionMapa?.let {
                    MapaDireccionPreview(
                        item = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                                onSelect = { viewModel.seleccionarPredeterminada(direccion.id) },
                                onEdit = { viewModel.editarDireccion(direccion.id) },
                                onDelete = { viewModel.solicitarEliminarDireccion(direccion.id) },
                                accionesHabilitadas = !state.eliminando
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
            onRegionChange = viewModel::actualizarRegion,
            onCiudadChange = viewModel::actualizarCiudad,
            onEtiquetaChange = viewModel::actualizarEtiqueta,
            onDireccionChange = viewModel::actualizarDireccion,
            onReferenciaChange = viewModel::actualizarReferencia,
            onOpenMap = { mostrarSelectorMapa = true },
            onPredeterminadaChange = viewModel::actualizarPredeterminada,
            onDismiss = viewModel::cerrarDialogo,
            onSave = viewModel::guardarDireccion
        )
    }

    if (state.confirmacionEliminarId != null) {
        ConfirmarEliminarDialog(
            eliminando = state.eliminando,
            onConfirm = viewModel::confirmarEliminacion,
            onDismiss = viewModel::cancelarEliminacion
        )
    }

    if (mostrarSelectorMapa) {
        SelectorMapaDialog(
            latitudInicial = state.formulario.latitud,
            longitudInicial = state.formulario.longitud,
            procesando = procesandoSeleccionMapa,
            onDismiss = {
                if (!procesandoSeleccionMapa) {
                    mostrarSelectorMapa = false
                }
            },
            onUbicacionConfirmada = { latitud, longitud ->
                scope.launch {
                    procesandoSeleccionMapa = true
                    try {
                        val direccionObtenida = geocoder.obtenerDireccion(latitud, longitud)
                        if (direccionObtenida == null) {
                            viewModel.mostrarMensaje("No encontramos una direccion exacta, completa los datos manualmente.")
                        }
                        viewModel.prepararFormularioConUbicacion(
                            direccion = direccionObtenida ?: "Ubicacion seleccionada en el mapa",
                            latitud = latitud,
                            longitud = longitud
                        )
                    } finally {
                        procesandoSeleccionMapa = false
                        mostrarSelectorMapa = false
                    }
                }
            }
        )
    }
}

@Composable
private fun DireccionItem(
    item: DireccionItemUi,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    accionesHabilitadas: Boolean
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
            IconButton(
                onClick = onEdit,
                enabled = accionesHabilitadas
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar direccion"
                )
            }
            IconButton(
                onClick = onDelete,
                enabled = accionesHabilitadas
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar direccion"
                )
            }
        }
    }
}

@Composable
private fun DireccionDialog(
    state: EnviarAUiState,
    onRegionChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onEtiquetaChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onReferenciaChange: (String) -> Unit,
    onOpenMap: () -> Unit,
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
                val latitudSeleccionada = state.formulario.latitud
                val longitudSeleccionada = state.formulario.longitud
                OutlinedTextField(
                    value = state.formulario.region,
                    onValueChange = onRegionChange,
                    label = { Text("Region") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.formulario.errorRegion != null,
                    supportingText = {
                        state.formulario.errorRegion?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                OutlinedTextField(
                    value = state.formulario.ciudad,
                    onValueChange = onCiudadChange,
                    label = { Text("Ciudad") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.formulario.errorCiudad != null,
                    supportingText = {
                        state.formulario.errorCiudad?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    }
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
                    value = state.formulario.etiqueta,
                    onValueChange = onEtiquetaChange,
                    label = { Text("Etiqueta (ej: Casa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.formulario.referencia,
                    onValueChange = onReferenciaChange,
                    label = { Text("Referencia (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.guardando
                ) {
                    Text("Seleccionar en el mapa")
                }
                if (latitudSeleccionada != null && longitudSeleccionada != null) {
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "Ubicacion seleccionada: %.5f, %.5f",
                            latitudSeleccionada,
                            longitudSeleccionada
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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

@Composable
private fun SelectorMapaDialog(
    latitudInicial: Double?,
    longitudInicial: Double?,
    procesando: Boolean,
    onDismiss: () -> Unit,
    onUbicacionConfirmada: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val puntoInicial = remember(latitudInicial, longitudInicial) {
        if (latitudInicial != null && longitudInicial != null) {
            crearPunto(latitudInicial, longitudInicial)
        } else {
            DEFAULT_POINT
        }
    }
    var puntoSeleccionado by remember(latitudInicial, longitudInicial) {
        mutableStateOf(
            if (latitudInicial != null && longitudInicial != null) {
                crearPunto(latitudInicial, longitudInicial)
            } else {
                null
            }
        )
    }
    val mapView = remember { MapView(context) }
    var styleListo by remember { mutableStateOf(false) }

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            styleListo = true
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(puntoInicial)
                    .zoom(DEFAULT_MAP_ZOOM)
                    .build()
            )
        }
        mapView.gestures.addOnMapClickListener { point ->
            puntoSeleccionado = point
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(DEFAULT_MAP_ZOOM)
                    .build()
            )
            true
        }
    }

    Dialog(
        onDismissRequest = { if (!procesando) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecciona un punto en el mapa",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Toca el mapa para elegir tu direccion exacta.",
                    style = MaterialTheme.typography.bodySmall
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { mapView }
                    )
                    if (styleListo) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Punto seleccionado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !procesando
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            puntoSeleccionado?.let { seleccion ->
                                onUbicacionConfirmada(
                                    seleccion.latitude(),
                                    seleccion.longitude()
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = puntoSeleccionado != null && !procesando
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Usar este punto")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapaDireccionPreview(
    item: DireccionItemUi,
    modifier: Modifier = Modifier
) {
    val latitud = item.latitud ?: return
    val longitud = item.longitud ?: return
    val punto = remember(latitud, longitud) { crearPunto(latitud, longitud) }
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var styleListo by remember { mutableStateOf(false) }

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            styleListo = true
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(punto)
                    .zoom(DEFAULT_MAP_ZOOM)
                    .build()
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Direccion seleccionada en el mapa",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { mapView }
                )
                if (styleListo) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Punto seleccionado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                    )
                }
            }
        }
        Text(
            text = item.direccion,
            style = MaterialTheme.typography.bodyMedium
        )
        item.referencia?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = "Referencia: $it",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private val DEFAULT_POINT: Point = Point.fromLngLat(-70.6693, -33.4489)
private const val DEFAULT_MAP_ZOOM = 16.0

private fun crearPunto(latitud: Double, longitud: Double): Point =
    Point.fromLngLat(longitud, latitud)

@Composable
private fun ConfirmarEliminarDialog(
    eliminando: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar direccion") },
        text = { Text("¿Deseas eliminar esta direccion?") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !eliminando) {
                if (eliminando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !eliminando) {
                Text("Cancelar")
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
