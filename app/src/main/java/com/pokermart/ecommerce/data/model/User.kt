package com.pokermart.ecommerce.data.model

/**
 * Informacion minima del usuario que inicia sesion en la aplicacion.
 */
data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String,
    val apellido: String? = null,
    val region: String? = null,
    val comuna: String? = null,
    val direccion: String? = null,
    val run: String? = null,
    val fechaNacimiento: String? = null,
    val fotoLocal: String? = null
)
