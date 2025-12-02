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
import java.text.NumberFormat
import java.util.Locale

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
    val subtotal: Double = 0.0,
    val impuesto: Double = 0.0,
    val envio: Double = 0.0,
    val servicio: Double = 0.0,
    val total: Double = 0.0,
    val cumpleMinimo: Boolean = true,
    val minimoCompra: Double = 0.0,
    val metodoPago: MetodoPago? = null,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val mostrarAccionDirecciones: Boolean = false
)

enum class MetodoPago(val etiqueta: String) {
    DEBITO("Debito"),
    CREDITO("Credito"),
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia")
}

class CartViewModel(
    private val repositorioCarrito: RepositorioCarrito,
    private val repositorioCatalogo: RepositorioCatalogo,
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val minimoCompra = 5000.0
    private val porcentajeImpuesto = 0.19
    private val tarifaServicio = 500.0

    private val _estado = MutableStateFlow(CartUiState())
    val estado = _estado.asStateFlow()

    private var usuarioId: Long? = null
    private var direccionPredeterminada: com.pokermart.ecommerce.data.model.Direccion? = null
    private var carritoActual: List<CarritoItem> = emptyList()

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
                    mensajeError = "Debes iniciar sesion para ver el carrito.",
                    minimoCompra = minimoCompra
                )
            }
            return
        }
        usuarioId = sesion.id
        _estado.update {
            it.copy(
                cargando = true,
                mensajeError = null,
                mensajeExito = null,
                minimoCompra = minimoCompra
            )
        }
        observarDireccionPredeterminada(sesion.id)
        observarCarrito(sesion.id)
    }

    private fun observarDireccionPredeterminada(uid: Long) {
        viewModelScope.launch {
            repositorioDirecciones.observarPredeterminada(uid).collectLatest { direccion ->
                direccionPredeterminada = direccion
                recalcularTotales(_estado.value.items)
            }
        }
    }

    private fun observarCarrito(uid: Long) {
        viewModelScope.launch {
            repositorioCarrito.observarCarrito(uid).collectLatest { items ->
                carritoActual = items
                val uiItems = runCatching {
                    coroutineScope {
                        items.map { item -> async { item.aUiItem() } }.awaitAll()
                    }
                }.getOrElse { error ->
                    val fallback = items.map { it.aUiFallback() }
                    val totalesFallback = calcularTotales(fallback)
                    _estado.update {
                        it.copy(
                            cargando = false,
                            items = fallback,
                            subtotal = totalesFallback.subtotal,
                            impuesto = totalesFallback.impuesto,
                            envio = totalesFallback.envio,
                            servicio = totalesFallback.servicio,
                            total = totalesFallback.total,
                            cumpleMinimo = totalesFallback.cumpleMinimo,
                            mensajeError = error.message ?: "No pudimos cargar el detalle de tus productos."
                        )
                    }
                    return@collectLatest
                }
                val totales = calcularTotales(uiItems)
                _estado.update {
                    it.copy(
                        cargando = false,
                        items = uiItems,
                        subtotal = totales.subtotal,
                        impuesto = totales.impuesto,
                        envio = totales.envio,
                        servicio = totales.servicio,
                        total = totales.total,
                        cumpleMinimo = totales.cumpleMinimo,
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
            if (!_estado.value.cumpleMinimo) {
                _estado.update {
                    it.copy(
                        mensajeError = "La compra minima es de ${formatearPrecio(minimoCompra)}.",
                        mensajeExito = null,
                        mostrarAccionDirecciones = false
                    )
                }
                return@launch
            }
            val stockOk = validarStockYActualizar()
            if (!stockOk) return@launch
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
                    subtotal = 0.0,
                    impuesto = 0.0,
                    envio = 0.0,
                    servicio = 0.0,
                    total = 0.0,
                    metodoPago = null,
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

    private fun CarritoItem.aUiFallback(): CartItemUi = CartItemUi(
        id = id,
        titulo = "Producto #$productoId",
        opcionNombre = opcionId?.let { "Opcion #$it" },
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        subtotal = precioUnitario * cantidad,
        imagenUrl = null
    )

    private data class Totales(
        val subtotal: Double,
        val impuesto: Double,
        val envio: Double,
        val servicio: Double,
        val total: Double,
        val cumpleMinimo: Boolean
    )

    private fun calcularTotales(items: List<CartItemUi>): Totales {
        val subtotal = items.sumOf { it.subtotal }
        val impuesto = subtotal * porcentajeImpuesto
        val envio = calcularEnvio(subtotal, direccionPredeterminada)
        val servicio = if (subtotal > 0) tarifaServicio else 0.0
        val total = subtotal + impuesto + envio + servicio
        val cumpleMinimo = subtotal >= minimoCompra
        return Totales(
            subtotal = subtotal,
            impuesto = impuesto,
            envio = envio,
            servicio = servicio,
            total = total,
            cumpleMinimo = cumpleMinimo
        )
    }

    private fun recalcularTotales(items: List<CartItemUi>) {
        val totales = calcularTotales(items)
        _estado.update {
            it.copy(
                subtotal = totales.subtotal,
                impuesto = totales.impuesto,
                envio = totales.envio,
                servicio = totales.servicio,
                total = totales.total,
                cumpleMinimo = totales.cumpleMinimo
            )
        }
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

    private fun formatearPrecio(valor: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        formato.maximumFractionDigits = 0
        formato.minimumFractionDigits = 0
        return formato.format(valor)
    }

    private fun calcularEnvio(subtotal: Double, direccion: com.pokermart.ecommerce.data.model.Direccion?): Double {
        if (subtotal <= 0.0) return 0.0
        if (subtotal >= 20000.0) return 0.0
        return when {
            direccion?.latitud != null && direccion.longitud != null -> 1500.0
            direccion != null -> 2000.0
            else -> 2500.0
        }
    }

    private suspend fun validarStockYActualizar(): Boolean {
        val ajustes = mutableListOf<String>()
        val sinStock = mutableListOf<String>()
        for (item in carritoActual) {
            val detalle = repositorioCatalogo.obtenerDetalleProducto(item.productoId)
            val opcion = detalle?.opciones?.firstOrNull { it.id == item.opcionId }
            val stockDisponible = opcion?.stock ?: Int.MAX_VALUE
            val nombre = detalle?.nombre ?: "Producto #${item.productoId}"
            when {
                stockDisponible <= 0 -> {
                    repositorioCarrito.eliminarItem(item.id)
                    sinStock += nombre
                }
                item.cantidad > stockDisponible -> {
                    repositorioCarrito.actualizarCantidad(item.id, stockDisponible)
                    ajustes += "$nombre ajustado a $stockDisponible por stock disponible."
                }
            }
        }
        if (sinStock.isNotEmpty() || ajustes.isNotEmpty()) {
            val mensajes = buildString {
                if (sinStock.isNotEmpty()) {
                    append("Sin stock: ${sinStock.joinToString(", ")}. ")
                }
                if (ajustes.isNotEmpty()) {
                    append(ajustes.joinToString(" "))
                }
            }
            _estado.update {
                it.copy(
                    mensajeError = mensajes.trim(),
                    mensajeExito = null,
                    mostrarAccionDirecciones = false
                )
            }
            return false
        }
        return true
    }
}
