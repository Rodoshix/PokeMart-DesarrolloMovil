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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
                        destinoLat = when (it.metodoEntrega) {
                            MetodoEntrega.RETIRO_TIENDA -> mallLat
                            MetodoEntrega.ENVIO -> dirs.firstOrNull { dir -> dir.id == it.direccionSeleccionadaId }?.latitud
                            null -> null
                        },
                        destinoLon = when (it.metodoEntrega) {
                            MetodoEntrega.RETIRO_TIENDA -> mallLon
                            MetodoEntrega.ENVIO -> dirs.firstOrNull { dir -> dir.id == it.direccionSeleccionadaId }?.longitud
                            null -> null
                        },
                        tiendaLat = mallLat,
                        tiendaLon = mallLon,
                        ruta = emptyList()
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
        _estado.update { actual ->
            val dir = direccionesActuales.firstOrNull { it.id == actual.direccionSeleccionadaId }
                ?: direccionesActuales.firstOrNull()
            val destinoSeleccionado: Pair<Double?, Double?> = if (metodo == MetodoEntrega.RETIRO_TIENDA) {
                Pair(mallLat, mallLon)
            } else {
                Pair(dir?.latitud, dir?.longitud)
            }
            val envio = calcularEnvio(actual.subtotal, metodo, actual.direccionSeleccionadaId)
            val total = actual.subtotal + actual.impuesto + envio + actual.servicio
            actual.copy(
                metodoEntrega = metodo,
                envio = envio,
                total = total,
                destinoLat = destinoSeleccionado.first,
                destinoLon = destinoSeleccionado.second,
                tiendaLat = mallLat,
                tiendaLon = mallLon,
                origenLat = null,
                origenLon = null,
                ruta = emptyList()
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
                destinoLat = if (it.metodoEntrega == MetodoEntrega.RETIRO_TIENDA) mallLat else lat,
                destinoLon = if (it.metodoEntrega == MetodoEntrega.RETIRO_TIENDA) mallLon else lon,
                origenLat = if (it.metodoEntrega == MetodoEntrega.ENVIO) mallLat else null,
                origenLon = if (it.metodoEntrega == MetodoEntrega.ENVIO) mallLon else null,
                ruta = emptyList()
            )
        }
    }

    fun actualizarOrigen(origen: LatLng?) {
        _estado.update {
            it.copy(
                origenLat = origen?.latitude,
                origenLon = origen?.longitude,
                ruta = emptyList()
            )
        }
    }

    fun solicitarRuta(origen: LatLng?, destino: LatLng?, apiKey: String) {
        if (origen == null || destino == null) return
        viewModelScope.launch {
            val puntos = obtenerRuta(origen, destino, apiKey)
            if (puntos != null) {
                _estado.update { it.copy(ruta = puntos, mensajeError = null) }
            } else {
                _estado.update { it.copy(mensajeError = "No pudimos calcular la ruta.") }
            }
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

    private suspend fun obtenerRuta(origen: LatLng, destino: LatLng, apiKey: String): List<LatLng>? =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    "https://maps.googleapis.com/maps/api/directions/json?origin=${origen.latitude},${origen.longitude}&destination=${destino.latitude},${destino.longitude}&mode=driving&key=$apiKey"
                val conexion = URL(url).openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"
                conexion.connectTimeout = 5000
                conexion.readTimeout = 5000
                val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(respuesta)
                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) return@withContext null
                val poly = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                decodePoly(poly)
            } catch (_: Exception) {
                null
            }
        }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng
            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }
        return poly
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
