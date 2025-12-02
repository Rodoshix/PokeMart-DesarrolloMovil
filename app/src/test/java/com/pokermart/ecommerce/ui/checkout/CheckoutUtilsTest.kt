package com.pokermart.ecommerce.ui.checkout

import com.pokermart.ecommerce.data.model.Direccion
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutUtilsTest {

    @Test
    fun `calcular envio gratis en retiro`() {
        val envio = CheckoutUtils.calcularEnvio(
            subtotal = 1000.0,
            metodoEntrega = MetodoEntrega.RETIRO_TIENDA,
            destino = null
        )
        assertEquals(0.0, envio, 0.0)
    }

    @Test
    fun `calcular envio con coords`() {
        val envio = CheckoutUtils.calcularEnvio(
            subtotal = 1000.0,
            metodoEntrega = MetodoEntrega.ENVIO,
            destino = Direccion(
                id = 1,
                usuarioId = 1,
                direccion = "Test",
                latitud = 1.0,
                longitud = 1.0
            )
        )
        assertEquals(1500.0, envio, 0.0)
    }

    @Test
    fun `decode polyline`() {
        val poly = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val puntos = CheckoutUtils.decodePoly(poly)
        val esperado = listOf(
            LatLng(38.5, -120.2),
            LatLng(40.7, -120.95),
            LatLng(43.252, -126.453)
        )
        assertEquals(esperado.size, puntos.size)
        esperado.zip(puntos).forEach { (exp, real) ->
            assertEquals(exp.latitude, real.latitude, 0.0001)
            assertEquals(exp.longitude, real.longitude, 0.0001)
        }
    }
}
