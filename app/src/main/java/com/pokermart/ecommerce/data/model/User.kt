package com.pokermart.ecommerce.data.model

/**
 * Informacion minima del usuario que inicia sesion en la aplicacion.
 */
data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String
)
