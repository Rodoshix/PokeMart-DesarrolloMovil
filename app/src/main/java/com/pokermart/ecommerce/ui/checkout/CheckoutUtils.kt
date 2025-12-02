package com.pokermart.ecommerce.ui.checkout

import com.google.android.gms.maps.model.LatLng
import com.pokermart.ecommerce.data.model.Direccion

object CheckoutUtils {

    fun calcularEnvio(
        subtotal: Double,
        metodoEntrega: MetodoEntrega?,
        destino: Direccion?
    ): Double {
        if (subtotal <= 0.0) return 0.0
        if (metodoEntrega == MetodoEntrega.RETIRO_TIENDA) return 0.0
        return when {
            destino?.latitud != null && destino.longitud != null -> 1500.0
            destino != null -> 2000.0
            else -> 2500.0
        }
    }

    fun decodePoly(encoded: String): List<LatLng> {
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
}
