package com.pokermart.ecommerce.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CategoryItem(
    val id: Long,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ProductItem(
    val id: Long,
    val title: String,
    val price: Double,
    val imageUrl: String
)

data class HomeUiState(
    val searchQuery: String = "",
    val address: String = "Debe ingresar Direccion",
    val carouselImages: List<String> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val snackbarMessage: String? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    repositorioCatalogo: RepositorioCatalogo,
    private val repositorioDirecciones: RepositorioDirecciones
) : ViewModel() {

    val destacados: Flow<List<ProductItem>> = repositorioCatalogo
        .observarDestacados()
        .map { productos ->
            productos.map { it.aProductItem() }
        }

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        cargarEstadoInicial()
        observarDireccionPredeterminada()
    }

    private fun cargarEstadoInicial() {
        uiState = uiState.copy(
            carouselImages = listOf(
                "https://oyster.ignimgs.com/mediawiki/apis.ign.com/pokemon-scarlet-violet/e/ef/Pokemon_Paldea_Map.jpg",
                "https://static.wikia.nocookie.net/pokemon/images/f/f8/Sinnoh_BDSP.png/revision/latest?cb=20210818192524",
                "https://nintendosoup.com/wp-content/uploads/2019/02/Pokemon_Sword-Shield-Galar_Map1-1038x576.jpg"
            ),
            categories = listOf(
                CategoryItem(1L, "Medicinas", Icons.Outlined.Storefront),
                CategoryItem(2L, "Poke Balls", Icons.Outlined.LocalOffer),
                CategoryItem(3L, "MTs y DTs", Icons.Outlined.Category)
            )
        )
    }

    private fun observarDireccionPredeterminada() {
        val usuarioId = sessionManager.obtenerSesion()?.id ?: run {
            uiState = uiState.copy(address = "Debe ingresar Direccion")
            return
        }
        viewModelScope.launch {
            repositorioDirecciones.observarPredeterminada(usuarioId).collectLatest { direccion ->
                uiState = uiState.copy(
                    address = direccion?.direccion ?: "Debe ingresar Direccion"
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onBellClick() {
        mostrarMensaje("Pronto veras tus notificaciones aqui.")
    }

    fun onChangeAddress() {
        // La navegacion se maneja en la capa de UI.
    }

    fun onCategoryClick(category: CategoryItem) {
        // Reservado para futuras acciones (analytics, tracking, etc.)
    }

    fun onProductClick(product: ProductItem) {
        mostrarMensaje("Producto seleccionado: ${product.title}")
    }

    fun consumirMensaje() {
        uiState = uiState.copy(snackbarMessage = null)
    }

    private fun mostrarMensaje(mensaje: String) {
        uiState = uiState.copy(snackbarMessage = mensaje)
    }

    private fun Producto.aProductItem(): ProductItem = ProductItem(
        id = id,
        title = nombre,
        price = precio,
        imageUrl = imagenUrl
    )
}

fun formatClp(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    format.minimumFractionDigits = 0
    return format.format(value)
}
