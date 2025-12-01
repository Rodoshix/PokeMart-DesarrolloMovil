package com.pokermart.ecommerce.ui.checkout

import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.ui.cart.MetodoPago
import com.google.android.gms.maps.model.LatLng

enum class MetodoEntrega { RETIRO_TIENDA, ENVIO }

data class CheckoutUiState(
    val cargando: Boolean = true,
    val subtotal: Double = 0.0,
    val impuesto: Double = 0.0,
    val envio: Double = 0.0,
    val servicio: Double = 0.0,
    val total: Double = 0.0,
    val metodoPago: MetodoPago? = null,
    val metodoEntrega: MetodoEntrega? = null,
    val direcciones: List<Direccion> = emptyList(),
    val direccionSeleccionadaId: Long? = null,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val destinoLat: Double? = null,
    val destinoLon: Double? = null,
    val origenLat: Double? = null,
    val origenLon: Double? = null,
    val ruta: List<LatLng> = emptyList()
)
