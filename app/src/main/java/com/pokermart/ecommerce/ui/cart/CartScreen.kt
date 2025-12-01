package com.pokermart.ecommerce.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pokermart.ecommerce.ui.common.EstadoVacio
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onVolver: () -> Unit,
    onIrProductos: () -> Unit,
    onIrDirecciones: () -> Unit,
    modifier: Modifier = Modifier
 ) {
    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensajeError, estado.mensajeExito) {
        estado.mensajeError?.let { mensaje ->
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = if (estado.mostrarAccionDirecciones) "Agregar direccion" else null
            )
            if (resultado == SnackbarResult.ActionPerformed && estado.mostrarAccionDirecciones) {
                onIrDirecciones()
            }
            viewModel.consumirMensajes()
        }
        estado.mensajeExito?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.consumirMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito") },
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
                            contentDescription = "Actualizar carrito"
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

            estado.mensajeError != null && estado.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVacio(
                        mensaje = estado.mensajeError ?: "No pudimos cargar tu carrito.",
                        accionTexto = "Reintentar",
                        enAccionClick = viewModel::recargar
                    )
                }
            }

            estado.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVacio(
                        mensaje = "Tu carrito esta vacio.",
                        accionTexto = "Explorar productos",
                        enAccionClick = onIrProductos
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estado.items) { item ->
                            CartItemCard(
                                item = item,
                                onIncrementar = { viewModel.incrementar(item.id) },
                                onDecrementar = { viewModel.decrementar(item.id) },
                                onEliminar = { viewModel.eliminar(item.id) }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = formatearPrecio(estado.total),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = viewModel::confirmarCompra,
                            enabled = estado.items.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar compra")
                        }
                        Button(
                            onClick = viewModel::vaciar,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vaciar carrito")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemUi,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imagenUrl,
                contentDescription = item.titulo,
                modifier = Modifier
                    .height(72.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                item.opcionNombre?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = formatearPrecio(item.precioUnitario),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subtotal: ${formatearPrecio(item.subtotal)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDecrementar) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Disminuir")
                    }
                    Text(
                        text = item.cantidad.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onIncrementar) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Aumentar")
                    }
                }
                IconButton(onClick = onEliminar) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

private fun formatearPrecio(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    formato.maximumFractionDigits = 0
    formato.minimumFractionDigits = 0
    return formato.format(valor)
}
