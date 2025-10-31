package com.pokermart.ecommerce.ui.login

import com.pokermart.ecommerce.data.model.Usuario

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val correoError: String? = null,
    val contrasenaError: String? = null,
    val mensajeErrorGeneral: String? = null,
    val cargando: Boolean = false,
    val usuarioAutenticado: Usuario? = null
)
