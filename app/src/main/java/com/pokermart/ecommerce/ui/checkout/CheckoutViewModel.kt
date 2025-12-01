package com.pokermart.ecommerce.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.ui.cart.MetodoPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val repositorioCarrito: RepositorioCarrito,
    private val repositorioCatalogo: RepositorioCatalogo,
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _estado = MutableStateFlow(CheckoutUiState())
    val estado = _estado.asStateFlow()

    private var usuarioId: Long? = null
    private var direccionesActuales: List<Direccion> = emptyList()

    private val mallLat = -33.52164
    private val mallLon = -70.59867

    init {
        cargar()
    }

    private fun cargar() {
        val sesion = sessionManager.obtenerSesion()
        if (sesion == null) {
            _estado.update {
                it.copy(
                    cargando = false,
                    mensajeError = "Debes iniciar sesion para finalizar la compra."
                )
            }
            return
        }
        usuarioId = sesion.id
        observarCarrito()
        observarDirecciones(sesion.id)
    }

    private fun observarDirecciones(uid: Long) {
        viewModelScope.launch {
            repositorioDirecciones.observarTodas(uid).collectLatest { dirs ->
                direccionesActuales = dirs
                _estado.update {
                    it.copy(
                        direcciones = dirs,
                        direccionSeleccionadaId = it.direccionSeleccionadaId ?: dirs.firstOrNull()?.id,
                        destinoLat = it.destinoLat ?: dirs.firstOrNull()?.latitud ?: mallLat,
                        destinoLon = it.destinoLon ?: dirs.firstOrNull()?.longitud ?: mallLon
                    )
                }
            }
        }
    }

    private fun observarCarrito() {
        viewModelScope.launch {
            val uid = usuarioId ?: return@launch
            repositorioCarrito.observarCarrito(uid).collectLatest { items ->
                val uiItems = items.map { it.aPrecioCalculado() }
                val subtotal = uiItems.sumOf { it.precioCalculado * it.cantidad }
                val impuesto = subtotal * 0.19
                val envio = calcularEnvio(subtotal, _estado.value.metodoEntrega, _estado.value.direccionSeleccionadaId)
                val servicio = if (subtotal > 0) 500.0 else 0.0
                val total = subtotal + impuesto + envio + servicio
                _estado.update {
                    it.copy(
                        cargando = false,
                        subtotal = subtotal,
                        impuesto = impuesto,
                        envio = envio,
                        servicio = servicio,
                        total = total,
                        mensajeError = null
                    )
                }
            }
        }
    }

    fun seleccionarPago(metodo: MetodoPago) {
        _estado.update { it.copy(metodoPago = metodo) }
    }

    fun seleccionarEntrega(metodo: MetodoEntrega) {
        _estado.update {
            val envio = calcularEnvio(it.subtotal, metodo, it.direccionSeleccionadaId)
            val total = it.subtotal + it.impuesto + envio + it.servicio
            it.copy(
                metodoEntrega = metodo,
                envio = envio,
                total = total,
                destinoLat = if (metodo == MetodoEntrega.RETIRO_TIENDA) mallLat else it.destinoLat,
                destinoLon = if (metodo == MetodoEntrega.RETIRO_TIENDA) mallLon else it.destinoLon
            )
        }
    }

    fun seleccionarDireccion(id: Long, lat: Double?, lon: Double?) {
        _estado.update {
            val envio = calcularEnvio(it.subtotal, it.metodoEntrega, id)
            val total = it.subtotal + it.impuesto + envio + it.servicio
            it.copy(
                direccionSeleccionadaId = id,
                envio = envio,
                total = total,
                destinoLat = lat ?: mallLat,
                destinoLon = lon ?: mallLon
            )
        }
    }

    fun confirmarCompra(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val uid = usuarioId ?: run {
                _estado.update { it.copy(mensajeError = "Debes iniciar sesion.") }
                return@launch
            }
            if (_estado.value.metodoPago == null) {
                _estado.update { it.copy(mensajeError = "Selecciona un metodo de pago.") }
                return@launch
            }
            val entrega = _estado.value.metodoEntrega
            if (entrega == null) {
                _estado.update { it.copy(mensajeError = "Elige retiro en tienda o envio.") }
                return@launch
            }
            if (entrega == MetodoEntrega.ENVIO) {
                val dir = direccionesActuales.firstOrNull { it.id == _estado.value.direccionSeleccionadaId }
                if (dir == null) {
                    _estado.update { it.copy(mensajeError = "Selecciona una direccion para el envio.") }
                    return@launch
                }
            }
            repositorioCarrito.limpiar(uid)
            _estado.update {
                it.copy(
                    mensajeError = null,
                    mensajeExito = "Compra confirmada. Preparando tu pedido.",
                    subtotal = 0.0,
                    impuesto = 0.0,
                    envio = 0.0,
                    servicio = 0.0,
                    total = 0.0
                )
            }
            onSuccess()
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensajeError = null, mensajeExito = null) }
    }

    private suspend fun com.pokermart.ecommerce.data.model.CarritoItem.aPrecioCalculado(): PrecioItem {
        val detalleProducto = runCatching {
            repositorioCatalogo.obtenerDetalleProducto(productoId)
        }.getOrNull()
        val opcion = detalleProducto?.opciones?.firstOrNull { it.id == opcionId }
        val precioCalculado = detalleProducto?.let { calcularPrecio(it, opcion) } ?: precioUnitario
        return PrecioItem(
            cantidad = cantidad,
            precioCalculado = precioCalculado
        )
    }

    private data class PrecioItem(val cantidad: Int, val precioCalculado: Double)

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

    private fun calcularEnvio(subtotal: Double, metodoEntrega: MetodoEntrega?, direccionId: Long?): Double {
        if (subtotal <= 0.0) return 0.0
        if (metodoEntrega == MetodoEntrega.RETIRO_TIENDA) return 0.0
        val destino = direccionesActuales.firstOrNull { it.id == direccionId }
        return when {
            destino?.latitud != null && destino.longitud != null -> 1500.0
            destino != null -> 2000.0
            else -> 2500.0
        }
    }
}
