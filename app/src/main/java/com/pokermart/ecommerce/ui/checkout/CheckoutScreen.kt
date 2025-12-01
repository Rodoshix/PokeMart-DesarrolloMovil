package com.pokermart.ecommerce.ui.checkout

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.pokermart.ecommerce.data.model.Direccion
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onManageAddresses: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        viewModel.estado.collectLatest { nuevo ->
            nuevo.mensajeExito?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensaje()
                onSuccess()
            }
            nuevo.mensajeError?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensaje()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (estado.cargando) {
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetodoPagoSection(
                    seleccionado = estado.metodoPago,
                    onSelect = viewModel::seleccionarPago
                )
                MetodoEntregaSection(
                    seleccionado = estado.metodoEntrega,
                    onSelect = viewModel::seleccionarEntrega,
                    onGestionarDirecciones = onManageAddresses
                )
                if (estado.metodoEntrega == MetodoEntrega.ENVIO) {
                    DireccionesSection(
                        direcciones = estado.direcciones,
                        seleccionadaId = estado.direccionSeleccionadaId,
                        onSelect = { dir ->
                            viewModel.seleccionarDireccion(dir.id, dir.latitud, dir.longitud)
                        }
                    )
                }
                MapaRuta(
                    lat = estado.destinoLat,
                    lon = estado.destinoLon,
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
                    ruta = estado.ruta,
                    metodoEntrega = estado.metodoEntrega,
                    tiendaLat = estado.tiendaLat,
                    tiendaLon = estado.tiendaLon,
                    onRutaNecesaria = { origen, destino, apiKey ->
                        viewModel.solicitarRuta(origen, destino, apiKey)
                    },
                    onOrigenDetectado = { origen ->
                        viewModel.actualizarOrigen(origen)
                    },
                    solicitarPermiso = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                )
                TotalesCard(
                    subtotal = estado.subtotal,
                    impuesto = estado.impuesto,
                    envio = estado.envio,
                    servicio = estado.servicio,
                    total = estado.total
                )
                Button(
                    onClick = { viewModel.confirmarCompra(onSuccess) },
                    enabled = estado.metodoPago != null && estado.metodoEntrega != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Text("Confirmar pedido", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun MetodoPagoSection(
    seleccionado: com.pokermart.ecommerce.ui.cart.MetodoPago?,
    onSelect: (com.pokermart.ecommerce.ui.cart.MetodoPago) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Metodo de pago", style = MaterialTheme.typography.titleMedium)
        com.pokermart.ecommerce.ui.cart.MetodoPago.values().forEach { metodo ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = seleccionado == metodo,
                    onClick = { onSelect(metodo) }
                )
                Text(text = metodo.etiqueta)
            }
        }
    }
}

@Composable
private fun MetodoEntregaSection(
    seleccionado: MetodoEntrega?,
    onSelect: (MetodoEntrega) -> Unit,
    onGestionarDirecciones: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Entrega", style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = seleccionado == MetodoEntrega.RETIRO_TIENDA,
                    onClick = { onSelect(MetodoEntrega.RETIRO_TIENDA) }
                )
                Text(text = "Retiro en tienda")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = seleccionado == MetodoEntrega.ENVIO,
                    onClick = { onSelect(MetodoEntrega.ENVIO) }
                )
                Text(text = "Envio a direccion")
            }
        }
        if (seleccionado == MetodoEntrega.ENVIO) {
            Button(onClick = onGestionarDirecciones) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Text(text = "Gestionar direcciones", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun DireccionesSection(
    direcciones: List<Direccion>,
    seleccionadaId: Long?,
    onSelect: (Direccion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Elige una direccion", style = MaterialTheme.typography.titleMedium)
        if (direcciones.isEmpty()) {
            Text(text = "No tienes direcciones guardadas.")
        } else {
            direcciones.forEach { dir ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = dir.direccion, style = MaterialTheme.typography.bodyMedium)
                        dir.referencia?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                    }
                    RadioButton(
                        selected = dir.id == seleccionadaId,
                        onClick = { onSelect(dir) }
                    )
                }
                Divider()
            }
        }
    }
}

@Composable
private fun MapaRuta(
    lat: Double?,
    lon: Double?,
    fusedLocationProviderClient: com.google.android.gms.location.FusedLocationProviderClient,
    ruta: List<LatLng>,
    metodoEntrega: MetodoEntrega?,
    tiendaLat: Double,
    tiendaLon: Double,
    onRutaNecesaria: (LatLng, LatLng, String) -> Unit,
    onOrigenDetectado: (LatLng?) -> Unit,
    solicitarPermiso: () -> Unit
) {
    val destino = if (lat != null && lon != null) LatLng(lat, lon) else null
    val context = LocalContext.current
    var origen by remember { mutableStateOf<LatLng?>(null) }
    val apiKey = stringResource(id = com.pokermart.ecommerce.R.string.google_maps_key)

    LaunchedEffect(destino, metodoEntrega) {
        if (metodoEntrega == MetodoEntrega.RETIRO_TIENDA) {
            // Origen es la ubicacion del usuario, destino tienda
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                val loc = fusedLocationProviderClient.awaitCurrentLocation()
                origen = loc?.let { LatLng(it.latitude, it.longitude) }
                onOrigenDetectado(origen)
                if (origen != null && destino != null) {
                    onRutaNecesaria(origen!!, destino, apiKey)
                }
            } else {
                onOrigenDetectado(null)
            }
        } else if (metodoEntrega == MetodoEntrega.ENVIO) {
            // Origen es la tienda, destino la direccion seleccionada
            origen = LatLng(tiendaLat, tiendaLon)
            onOrigenDetectado(origen)
            if (destino != null) {
                onRutaNecesaria(origen!!, destino, apiKey)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Ruta estimada", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = solicitarPermiso) {
                Icon(Icons.Default.Map, contentDescription = "Permitir ubicacion")
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (destino == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecciona una direccion o retiro en tienda.")
                }
            } else {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(destino, 13f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true,
                        myLocationButtonEnabled = false
                    )
                ) {
                    Marker(state = rememberMarkerState(position = destino), title = "Destino")
                    origen?.let { orig ->
                        Marker(state = rememberMarkerState(position = orig), title = if (metodoEntrega == MetodoEntrega.RETIRO_TIENDA) "Tu ubicacion" else "Tienda")
                        val puntos = if (ruta.isNotEmpty()) ruta else listOf(orig, destino)
                        Polyline(points = puntos)
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalesCard(
    subtotal: Double,
    impuesto: Double,
    envio: Double,
    servicio: Double,
    total: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TotalesRow(label = "Subtotal", valor = subtotal)
            TotalesRow(label = "Impuestos (19%)", valor = impuesto)
            TotalesRow(label = "Envio estimado", valor = envio)
            TotalesRow(label = "Tarifa de servicio", valor = servicio)
            Divider()
            TotalesRow(label = "Total", valor = total, negrita = true)
        }
    }
}

@Composable
private fun TotalesRow(label: String, valor: Double, negrita: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (negrita) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formatCurrency(valor),
            style = if (negrita) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium,
            color = if (negrita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatCurrency(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    formato.maximumFractionDigits = 0
    formato.minimumFractionDigits = 0
    return formato.format(valor)
}

private suspend fun com.google.android.gms.location.FusedLocationProviderClient.awaitCurrentLocation(): Location? =
    suspendCancellableCoroutine { continuation ->
        val tokenSource = CancellationTokenSource()
        val task = this.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
        task.addOnSuccessListener { location ->
            if (continuation.isActive) continuation.resume(location)
        }
        task.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
        continuation.invokeOnCancellation { tokenSource.cancel() }
    }
