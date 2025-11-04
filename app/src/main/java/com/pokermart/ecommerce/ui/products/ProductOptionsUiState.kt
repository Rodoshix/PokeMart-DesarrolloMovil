package com.pokermart.ecommerce.ui.products

import com.pokermart.ecommerce.data.model.Producto

data class ProductOptionsUiState(
    val cargando: Boolean = true,
    val producto: Producto? = null,
    val mensajeError: String? = null,
    val mensajeCompra: String? = null,
    val errorCompra: String? = null,
    val mostrarAccionIrPerfil: Boolean = false,
    val opcionSeleccionadaId: Long? = null
)
