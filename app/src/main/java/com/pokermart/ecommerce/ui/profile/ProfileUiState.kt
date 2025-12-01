package com.pokermart.ecommerce.ui.profile

data class ProfileUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val nombre: String = "",
    val apellido: String = "",
    val region: String = "",
    val comuna: String = "",
    val direccion: String = "",
    val run: String = "",
    val fechaNacimiento: String? = null,
    val correo: String = "",
    val fotoActual: String? = null,
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorRun: String? = null,
    val errorFechaNacimiento: String? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)
