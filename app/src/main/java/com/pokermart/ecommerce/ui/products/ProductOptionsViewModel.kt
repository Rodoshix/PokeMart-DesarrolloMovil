package com.pokermart.ecommerce.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val ARG_PRODUCTO_ID = "productoId"

class ProductOptionsViewModel(
    private val repositorioCatalogo: RepositorioCatalogo,
    private val productoId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(ProductOptionsUiState())
    val estado = _estado.asStateFlow()

    init {
        cargarProducto()
    }

    fun recargar() {
        cargarProducto()
    }

    private fun cargarProducto() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensajeError = null)
            val producto = runCatching { repositorioCatalogo.obtenerDetalleProducto(productoId) }
                .getOrElse { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeError = error.message ?: "Error al cargar el producto."
                    )
                    return@launch
                }
            if (producto == null) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    mensajeError = "No encontramos este producto."
                )
            } else {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    producto = producto,
                    mensajeError = null
                )
            }
        }
    }
}
