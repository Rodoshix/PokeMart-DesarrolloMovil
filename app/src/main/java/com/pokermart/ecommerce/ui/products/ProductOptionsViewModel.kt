package com.pokermart.ecommerce.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ARG_PRODUCTO_ID = "productoId"

class ProductOptionsViewModel(
    private val repositorioCatalogo: RepositorioCatalogo,
    private val repositorioCarrito: RepositorioCarrito,
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

    fun agregarAlCarrito() {
        val productoActual = _estado.value.producto ?: return
        viewModelScope.launch {
            val opcionSeleccionadaId = _estado.value.opcionSeleccionadaId ?: run {
                _estado.update {
                    it.copy(
                        errorCompra = "Selecciona una opcion antes de agregar.",
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
                        errorCompra = "Debes iniciar sesion para agregar al carrito.",
                        mostrarAccionIrPerfil = false
                    )
                }
                return@launch
            }
            val opcionSeleccionada = productoActual.opciones.firstOrNull { it.id == opcionSeleccionadaId }
            val precioUnitario = calcularPrecioFinal(productoActual, opcionSeleccionada)

            runCatching {
                repositorioCarrito.agregarOIncrementar(
                    usuarioId = sesion.id,
                    productoId = productoActual.id,
                    opcionId = opcionSeleccionadaId,
                    precioUnitario = precioUnitario
                )
            }.onSuccess {
                _estado.update {
                    val opcionNombre = opcionSeleccionada?.nombre
                    it.copy(
                        mensajeCompra = if (opcionNombre != null) {
                            "Agregado al carrito: ${productoActual.nombre} ($opcionNombre)."
                        } else {
                            "Agregado al carrito: ${productoActual.nombre}."
                        },
                        errorCompra = null,
                        mostrarAccionIrPerfil = false
                    )
                }
            }.onFailure { error ->
                _estado.update {
                    it.copy(
                        mensajeCompra = null,
                        errorCompra = error.message ?: "No pudimos agregar el producto al carrito.",
                        mostrarAccionIrPerfil = false
                    )
                }
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

    private fun calcularPrecioFinal(
        producto: Producto,
        opcion: OpcionProducto?
    ): Double {
        val cantidad = opcion?.let { extraerCantidad(it.nombre) } ?: 1
        val extra = opcion?.precioExtra ?: 0.0
        return (producto.precio * cantidad) + extra
    }

    private fun extraerCantidad(nombre: String): Int? {
        val regex = Regex("x\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(nombre)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
}
