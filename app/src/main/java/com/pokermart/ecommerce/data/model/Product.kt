package com.pokermart.ecommerce.data.model

/**
 * Modelo de dominio para un producto de la tienda.
 */
data class Producto(
    val id: Long,
    val categoriaId: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String,
    val destacado: Boolean,
    val opciones: List<OpcionProducto>
)
