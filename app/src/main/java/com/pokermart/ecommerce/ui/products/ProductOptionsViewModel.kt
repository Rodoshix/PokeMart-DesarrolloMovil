package com.pokermart.ecommerce.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ARG_PRODUCTO_ID = "productoId"

class ProductOptionsViewModel(
    private val repositorioCatalogo: RepositorioCatalogo,
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager,
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
            _estado.value = _estado.value.copy(
                cargando = true,
                mensajeError = null,
                mensajeCompra = null,
                errorCompra = null,
                mostrarAccionIrPerfil = false
            )
            val producto = runCatching { repositorioCatalogo.obtenerDetalleProducto(productoId) }
                .getOrElse { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeError = error.message ?: "Error al cargar el producto.",
                        opcionSeleccionadaId = null,
                        mensajeCompra = null,
                        errorCompra = null,
                        mostrarAccionIrPerfil = false
                    )
                    return@launch
                }
            if (producto == null) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    mensajeError = "No encontramos este producto.",
                    opcionSeleccionadaId = null,
                    mensajeCompra = null,
                    errorCompra = null,
                    mostrarAccionIrPerfil = false
                )
            } else {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    producto = producto,
                    mensajeError = null,
                    opcionSeleccionadaId = null,
                    mensajeCompra = null,
                    errorCompra = null,
                    mostrarAccionIrPerfil = false
                )
            }
        }
    }

    fun comprarProducto() {
        val productoActual = _estado.value.producto ?: return
        viewModelScope.launch {
            val opcionSeleccionadaId = _estado.value.opcionSeleccionadaId ?: run {
                _estado.update {
                    it.copy(
                        errorCompra = "Selecciona una opcion antes de comprar.",
                        mensajeCompra = null,
                        mostrarAccionIrPerfil = false
                    )
                }
                return@launch
            }
            val sesion = sessionManager.obtenerSesion()
            if (sesion == null) {
                _estado.update {
                    it.copy(
                        mensajeCompra = null,
                        errorCompra = "Debes iniciar sesion para comprar.",
                        mostrarAccionIrPerfil = false
                    )
                }
                return@launch
            }
            val direccionPredeterminada = repositorioDirecciones
                .observarPredeterminada(sesion.id)
                .firstOrNull()

            if (direccionPredeterminada == null) {
                _estado.update {
                    it.copy(
                        mensajeCompra = null,
                        errorCompra = "Necesitas registrar una direccion de envio antes de comprar.",
                        mostrarAccionIrPerfil = true
                    )
                }
                return@launch
            }

            val datosCompletos = sesion.apellido?.isNotBlank() == true &&
                sesion.region?.isNotBlank() == true &&
                sesion.comuna?.isNotBlank() == true &&
                sesion.run?.isNotBlank() == true &&
                sesion.fechaNacimiento?.isNotBlank() == true

            if (!datosCompletos) {
                _estado.update {
                    it.copy(
                        mensajeCompra = null,
                        errorCompra = "Completa los datos de tu perfil antes de comprar.",
                        mostrarAccionIrPerfil = true
                    )
                }
                return@launch
            }

            _estado.update {
                val opcionNombre = productoActual.opciones.firstOrNull { it.id == opcionSeleccionadaId }?.nombre
                it.copy(
                    mensajeCompra = if (opcionNombre != null) {
                        "Compra realizada con exito. Prepararemos tu pedido de ${productoActual.nombre} ($opcionNombre)."
                    } else {
                        "Compra realizada con exito. Prepararemos tu pedido de ${productoActual.nombre}."
                    },
                    errorCompra = null,
                    mostrarAccionIrPerfil = false
                )
            }
        }
    }

    fun limpiarMensajeCompra() {
        _estado.update {
            it.copy(mensajeCompra = null, errorCompra = null, mostrarAccionIrPerfil = false)
        }
    }

    fun seleccionarOpcion(opcionId: Long) {
        _estado.update {
            it.copy(
                opcionSeleccionadaId = opcionId,
                mensajeCompra = null,
                errorCompra = null,
                mostrarAccionIrPerfil = false
            )
        }
    }
}
