package com.pokermart.ecommerce.ui.register

import com.pokermart.ecommerce.data.model.Usuario

data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val correoError: String? = null,
    val contrasenaError: String? = null,
    val confirmacionError: String? = null,
    val mensajeErrorGeneral: String? = null,
    val cargando: Boolean = false,
    val usuarioRegistrado: Usuario? = null
)
