package com.pokermart.ecommerce.data.model

/**
 * Variacion disponible para un producto especifico.
 */
data class OpcionProducto(
    val id: Long,
    val productoId: Long,
    val nombre: String,
    val descripcion: String,
    val precioExtra: Double,
    val stock: Int
)
