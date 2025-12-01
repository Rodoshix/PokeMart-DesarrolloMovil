package com.pokermart.ecommerce.data.model

/**
 * Item dentro del carrito de compras asociado a un usuario.
 */
data class CarritoItem(
    val id: Long = 0,
    val usuarioId: Long,
    val productoId: Long,
    val opcionId: Long? = null,
    val cantidad: Int,
    val precioUnitario: Double,
    val agregadoEl: Long = System.currentTimeMillis()
)
