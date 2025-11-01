package com.pokermart.ecommerce.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pokermart.ecommerce.pref.SessionManager
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
    val price: Long,
    val imageUrl: String
)

data class HomeUiState(
    val searchQuery: String = "",
    val address: String = "Los Quillayes 717",
    val carouselImages: List<String> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val featuredProducts: List<ProductItem> = emptyList(),
    val snackbarMessage: String? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        cargarEstadoInicial()
    }

    private fun cargarEstadoInicial() {
        val direccionGuardada = sessionManager.obtenerSesion()?.direccion
        uiState = uiState.copy(
            address = direccionGuardada?.takeIf { it.isNotBlank() } ?: "Los Quillayes 717",
            carouselImages = listOf(
                "https://oyster.ignimgs.com/mediawiki/apis.ign.com/pokemon-scarlet-violet/e/ef/Pokemon_Paldea_Map.jpg",
                "https://static.wikia.nocookie.net/pokemon/images/f/f8/Sinnoh_BDSP.png/revision/latest?cb=20210818192524",
                "https://nintendosoup.com/wp-content/uploads/2019/02/Pokemon_Sword-Shield-Galar_Map1-1038x576.jpg"
            ),
            categories = listOf(
                CategoryItem(1L, "Medicinas", Icons.Outlined.Storefront),
                CategoryItem(2L, "Poke Balls", Icons.Outlined.LocalOffer),
                CategoryItem(3L, "MTs y DTs", Icons.Outlined.Category)
            ),
            featuredProducts = mockProductos()
        )
    }

    fun onSearchChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onBellClick() {
        mostrarMensaje("Pronto veras tus notificaciones aqui.")
    }

    fun onChangeAddress() {
        mostrarMensaje("Funcionalidad de direccion en desarrollo.")
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

    private fun mockProductos(): List<ProductItem> = listOf(
        ProductItem(
            id = 100,
            title = "Poke Ball Premium",
            price = 5_490,
            imageUrl = "https://assets.pokemon.com/assets/cms2/img/items/poke-ball.png"
        ),
        ProductItem(
            id = 101,
            title = "Ultra Ball Pro",
            price = 11_990,
            imageUrl = "https://assets.pokemon.com/assets/cms2/img/items/ultra-ball.png"
        ),
        ProductItem(
            id = 102,
            title = "Superpocion Plus",
            price = 8_990,
            imageUrl = "https://assets.pokemon.com/assets/cms2/img/items/super-potion.png"
        ),
        ProductItem(
            id = 103,
            title = "MT Trueno",
            price = 24_990,
            imageUrl = "https://assets.pokemon.com/assets/cms2/img/items/tm.png"
        )
    )
}

fun formatClp(value: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(value)
}
