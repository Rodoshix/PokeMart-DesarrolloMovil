package com.pokermart.ecommerce.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pokermart.ecommerce.data.model.Categoria
import com.pokermart.ecommerce.ui.common.EstadoVacio
import com.pokermart.ecommerce.ui.common.TarjetaCategoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onCategoriaSeleccionada: (Categoria) -> Unit,
    onPerfilClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val mensajeError = estado.mensajeError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorias") },
                actions = {
                    IconButton(onClick = onPerfilClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Ir a tu perfil"
                        )
                    }
                    IconButton(onClick = viewModel::refrescar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar categorias"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
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
                        enAccionClick = viewModel::refrescar
                    )
                }
            }

            estado.categorias.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVacio(mensaje = "Aun no hay categorias cargadas.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.categorias) { categoria ->
                        TarjetaCategoria(
                            categoria = categoria,
                            enClick = onCategoriaSeleccionada,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
