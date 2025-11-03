package com.pokermart.ecommerce.ui.products

import com.pokermart.ecommerce.data.model.Producto

data class ProductOptionsUiState(
    val cargando: Boolean = true,
    val producto: Producto? = null,
    val mensajeError: String? = null
)
