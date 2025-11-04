package com.pokermart.ecommerce.ui.address

data class DireccionItemUi(
    val id: Long,
    val etiqueta: String?,
    val direccion: String,
    val referencia: String?,
    val esPredeterminada: Boolean,
    val latitud: Double?,
    val longitud: Double?
)

data class DireccionFormState(
    val id: Long? = null,
    val etiqueta: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val marcarComoPredeterminada: Boolean = false,
    val errorDireccion: String? = null
)

data class EnviarAUiState(
    val cargando: Boolean = true,
    val direcciones: List<DireccionItemUi> = emptyList(),
    val mostrarDialogo: Boolean = false,
    val formulario: DireccionFormState = DireccionFormState(),
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val confirmacionEliminarId: Long? = null,
    val eliminando: Boolean = false
)
