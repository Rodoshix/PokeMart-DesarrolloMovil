package com.pokermart.ecommerce.data.model

data class Direccion(
    val id: Long = 0,
    val usuarioId: Long,
    val etiqueta: String? = null,
    val direccion: String,
    val referencia: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val esPredeterminada: Boolean = false,
    val creadoEl: Long = System.currentTimeMillis()
)
