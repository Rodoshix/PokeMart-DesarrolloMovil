package com.pokermart.ecommerce.ui.products

import com.pokermart.ecommerce.data.model.Producto

data class ProductListUiState(
    val tituloCategoria: String = "Productos",
    val cargando: Boolean = true,
    val productos: List<Producto> = emptyList(),
    val mensajeError: String? = null
)
