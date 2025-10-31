package com.pokermart.ecommerce.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

const val ARG_CATEGORIA_ID = "categoriaId"
const val ARG_CATEGORIA_NOMBRE = "categoriaNombre"

class ProductsViewModel(
    private val repositorioCatalogo: RepositorioCatalogo,
    private val categoriaId: Long,
    categoriaNombre: String
) : ViewModel() {

    private val _estado = MutableStateFlow(ProductListUiState(tituloCategoria = categoriaNombre))
    val estado = _estado.asStateFlow()

    private var observacionJob: Job? = null

    init {
        observarProductos()
    }

    fun refrescar() {
        observarProductos()
    }

    private fun observarProductos() {
        observacionJob?.cancel()
        observacionJob = viewModelScope.launch {
            repositorioCatalogo
                .observarProductosPorCategoria(categoriaId)
                .onStart {
                    _estado.value = _estado.value.copy(cargando = true, mensajeError = null)
                }
                .catch { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeError = error.message ?: "Error al obtener productos."
                    )
                }
                .collectLatest { productos ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        productos = productos,
                        mensajeError = null
                    )
                }
        }
    }
}
