package com.pokermart.ecommerce.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.CarritoItem
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItemUi(
    val id: Long,
    val titulo: String,
    val opcionNombre: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val imagenUrl: String?
)

data class CartUiState(
    val cargando: Boolean = true,
    val items: List<CartItemUi> = emptyList(),
    val total: Double = 0.0,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val mostrarAccionDirecciones: Boolean = false
)

class CartViewModel(
    private val repositorioCarrito: RepositorioCarrito,
    private val repositorioCatalogo: RepositorioCatalogo,
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _estado = MutableStateFlow(CartUiState())
    val estado = _estado.asStateFlow()

    private var usuarioId: Long? = null

    init {
        cargarCarrito()
    }

    fun recargar() {
        cargarCarrito()
    }

    private fun cargarCarrito() {
        val sesion = sessionManager.obtenerSesion()
        if (sesion == null) {
            _estado.update {
                it.copy(
                    cargando = false,
                    mensajeError = "Debes iniciar sesion para ver el carrito."
                )
            }
            return
        }
        usuarioId = sesion.id
        _estado.update { it.copy(cargando = true, mensajeError = null, mensajeExito = null) }
        observarCarrito(sesion.id)
    }

    private fun observarCarrito(uid: Long) {
        viewModelScope.launch {
            repositorioCarrito.observarCarrito(uid).collectLatest { items ->
                val uiItems = coroutineScope {
                    items.map { item -> async { item.aUiItem() } }.awaitAll()
                }
                val total = uiItems.sumOf { it.subtotal }
                _estado.update {
                    it.copy(
                        cargando = false,
                        items = uiItems,
                        total = total,
                        mensajeError = null
                    )
                }
            }
        }
    }

    fun incrementar(itemId: Long) {
        val actual = _estado.value.items.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            repositorioCarrito.actualizarCantidad(itemId, actual.cantidad + 1)
        }
    }

    fun decrementar(itemId: Long) {
        val actual = _estado.value.items.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            if (actual.cantidad <= 1) {
                repositorioCarrito.eliminarItem(itemId)
            } else {
                repositorioCarrito.actualizarCantidad(itemId, actual.cantidad - 1)
            }
        }
    }

    fun eliminar(itemId: Long) {
        viewModelScope.launch {
            repositorioCarrito.eliminarItem(itemId)
        }
    }

    fun vaciar() {
        val uid = usuarioId ?: return
        viewModelScope.launch {
            repositorioCarrito.limpiar(uid)
        }
    }

    fun confirmarCompra() {
        viewModelScope.launch {
            val uid = usuarioId ?: sessionManager.obtenerSesion()?.id
            val sesion = sessionManager.obtenerSesion()
            if (uid == null || sesion == null) {
                _estado.update {
                    it.copy(
                        mensajeError = "Debes iniciar sesion para comprar.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = false
                    )
                }
                return@launch
            }
            if (_estado.value.items.isEmpty()) {
                _estado.update {
                    it.copy(
                        mensajeError = "Tu carrito esta vacio.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = false
                    )
                }
                return@launch
            }
            val direccion = repositorioDirecciones.observarPredeterminada(uid).firstOrNull()
            if (direccion == null) {
                _estado.update {
                    it.copy(
                        mensajeError = "Necesitas agregar una direccion antes de comprar.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = true
                    )
                }
                return@launch
            }
            val runCompleto = sesion.run?.isNotBlank() == true
            val nombreCompleto = sesion.nombre.isNotBlank() && sesion.apellido?.isNotBlank() == true
            val fechaCompleta = sesion.fechaNacimiento?.isNotBlank() == true
            if (!runCompleto) {
                _estado.update {
                    it.copy(
                        mensajeError = "Completa tu RUN en el perfil antes de comprar.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = false
                    )
                }
                return@launch
            }
            if (!nombreCompleto || !fechaCompleta) {
                _estado.update {
                    it.copy(
                        mensajeError = "Completa tu perfil (nombre, apellido y fecha de nacimiento) antes de comprar.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = false
                    )
                }
                return@launch
            }

            repositorioCarrito.limpiar(uid)
            _estado.update {
                it.copy(
                    items = emptyList(),
                    total = 0.0,
                    mensajeExito = "Compra realizada con exito. Enviaremos tu pedido a ${direccion.direccion}.",
                    mensajeError = null,
                    mostrarAccionDirecciones = false
                )
            }
        }
    }

    fun consumirMensajes() {
        _estado.update { it.copy(mensajeError = null, mensajeExito = null, mostrarAccionDirecciones = false) }
    }

    private suspend fun CarritoItem.aUiItem(): CartItemUi {
        val detalleProducto = runCatching {
            repositorioCatalogo.obtenerDetalleProducto(productoId)
        }.getOrNull()
        val opcion = detalleProducto?.opciones?.firstOrNull { it.id == opcionId }
        val titulo = detalleProducto?.nombre ?: "Producto #$productoId"
        val opcionNombre = opcion?.nombre ?: opcionId?.let { "Opcion #$it" }
        val precioCalculado = detalleProducto?.let { calcularPrecio(it, opcion) } ?: precioUnitario
        val subtotal = precioCalculado * cantidad
        return CartItemUi(
            id = id,
            titulo = titulo,
            opcionNombre = opcionNombre,
            cantidad = cantidad,
            precioUnitario = precioCalculado,
            subtotal = subtotal,
            imagenUrl = detalleProducto?.imagenUrl
        )
    }

    private fun calcularPrecio(producto: Producto, opcion: OpcionProducto?): Double {
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
