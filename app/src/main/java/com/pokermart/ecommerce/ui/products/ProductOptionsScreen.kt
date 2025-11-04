package com.pokermart.ecommerce.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pokermart.ecommerce.ui.common.EstadoVacio
import com.pokermart.ecommerce.ui.common.TarjetaOpcionProducto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductOptionsScreen(
    viewModel: ProductOptionsViewModel,
    onVolver: () -> Unit,
    onIrPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val mensajeError = estado.mensajeError
    val producto = estado.producto
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensajeCompra) {
        val mensaje = estado.mensajeCompra ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajeCompra()
    }

    LaunchedEffect(estado.errorCompra, estado.mostrarAccionIrPerfil) {
        val mensaje = estado.errorCompra ?: return@LaunchedEffect
        val resultado = snackbarHostState.showSnackbar(
            message = mensaje,
            actionLabel = if (estado.mostrarAccionIrPerfil) "Ir al perfil" else null
        )
        if (resultado == SnackbarResult.ActionPerformed && estado.mostrarAccionIrPerfil) {
            onIrPerfil()
        }
        viewModel.limpiarMensajeCompra()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto?.nombre ?: "Detalle de producto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::recargar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when {
            estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            mensajeError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVacio(
                        mensaje = mensajeError,
                        accionTexto = "Reintentar",
                        enAccionClick = viewModel::recargar
                    )
                }
            }

            producto == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    EstadoVacio(mensaje = "No encontramos informacion para este producto.")
                }
            }

            else -> {
                val productoSeleccionado = producto
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = productoSeleccionado.imagenUrl,
                                contentDescription = productoSeleccionado.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = productoSeleccionado.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = productoSeleccionado.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Precio base: $${String.format("%.2f", productoSeleccionado.precio)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    item {
                    Text(
                        text = "Opciones disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                items(productoSeleccionado.opciones) { opcion ->
                    TarjetaOpcionProducto(
                        opcion = opcion,
                        precioBase = productoSeleccionado.precio,
                        seleccionado = estado.opcionSeleccionadaId == opcion.id,
                        onSeleccionar = { viewModel.seleccionarOpcion(opcion.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Button(
                        onClick = viewModel::comprarProducto,
                        enabled = estado.opcionSeleccionadaId != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Comprar")
                    }
                }
            }
        }
    }
}
}
