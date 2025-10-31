package com.pokermart.ecommerce.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repositorioCatalogo: RepositorioCatalogo
) : ViewModel() {

    private val _estado = MutableStateFlow(CategoriesUiState())
    val estado = _estado.asStateFlow()
    private var observacionJob: Job? = null

    init {
        observarCategorias()
    }

    fun refrescar() {
        observarCategorias()
    }

    private fun observarCategorias() {
        observacionJob?.cancel()
        observacionJob = viewModelScope.launch {
            repositorioCatalogo
                .observarCategorias()
                .catch { error ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeError = error.message ?: "Error al cargar categorias."
                    )
                }
                .collectLatest { categorias ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        categorias = categorias,
                        mensajeError = null
                    )
                }
        }
    }
}
