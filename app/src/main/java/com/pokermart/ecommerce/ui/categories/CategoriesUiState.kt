package com.pokermart.ecommerce.ui.categories

import com.pokermart.ecommerce.data.model.Categoria

data class CategoriesUiState(
    val cargando: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val mensajeError: String? = null
)
