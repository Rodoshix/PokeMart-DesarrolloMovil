package com.pokermart.ecommerce.data.model

/**
 * Representa una categoria disponible en el catalogo de PokeMart.
 */
data class Categoria(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String
)
