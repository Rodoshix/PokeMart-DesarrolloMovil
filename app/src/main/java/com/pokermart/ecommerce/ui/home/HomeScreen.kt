package com.pokermart.ecommerce.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.pokermart.ecommerce.ui.common.EstadoVacio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    featuredProducts: List<ProductItem>,
    onSearchChange: (String) -> Unit,
    onBellClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChangeAddress: () -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
    onProductClick: (ProductItem) -> Unit,
    onDismissMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onDismissMessage()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = state.searchQuery,
                address = state.address,
                cartCount = state.cartCount,
                onSearchChange = onSearchChange,
                onBellClick = onBellClick,
                onCartClick = onCartClick,
                onProfileClick = onProfileClick,
                onChangeAddress = onChangeAddress
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            val estaBuscando = state.searchQuery.isNotBlank()
            if (estaBuscando) {
                Text(
                    text = "Resultados de \"${state.searchQuery}\"",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                if (state.searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EstadoVacio(mensaje = "No encontramos productos que coincidan con tu busqueda.")
                    }
                } else {
                    ProductGrid(
                        productos = state.searchResults,
                        onProductClick = onProductClick,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                }
            } else {
                HomeCarousel(images = state.carouselImages)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Categorias",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                CategoryRow(
                    categories = state.categories,
                    onCategoryClick = onCategoryClick
                )
                if (featuredProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Destacados",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    ProductGrid(
                        productos = featuredProducts,
                        onProductClick = onProductClick,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    searchQuery: String,
    address: String,
    cartCount: Int,
    onSearchChange: (String) -> Unit,
    onBellClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChangeAddress: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar en PokeMart") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            actions = {
                IconButton(onClick = onCartClick) {
                    if (cartCount > 0) {
                        BadgedBox(badge = {
                            Badge {
                                Text(text = cartCount.coerceAtMost(99).toString())
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Ver carrito"
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver carrito"
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Ir al perfil"
                    )
                }
                IconButton(onClick = onBellClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enviar a",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                }
                IconButton(onClick = onChangeAddress) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Cambiar direccion"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeCarousel(images: List<String>) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size })

    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            val imageUrl = images[page]
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen promocional ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(
    categories: List<CategoryItem>,
    onCategoryClick: (CategoryItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    productos: List<ProductItem>,
    onProductClick: (ProductItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(productos) { producto ->
            ProductCard(
                producto = producto,
                onClick = { onProductClick(producto) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    producto: ProductItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = producto.imageUrl,
                    contentDescription = producto.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = producto.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Text(
                text = formatClp(producto.price),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onChangeAddress: () -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onProductClick: (ProductItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val state = viewModel.uiState
    val featuredProducts by viewModel.destacados.collectAsState(initial = emptyList())
    HomeScreen(
        state = state,
        featuredProducts = featuredProducts,
        onSearchChange = viewModel::onSearchChange,
        onBellClick = viewModel::onBellClick,
        onCartClick = onCartClick,
        onProfileClick = onProfileClick,
        onChangeAddress = {
            viewModel.onChangeAddress()
            onChangeAddress()
        },
        onCategoryClick = { category ->
            viewModel.onCategoryClick(category)
            onCategoryClick(category)
        },
        onProductClick = { product ->
            viewModel.onProductClick(product)
            onProductClick(product)
        },
        onDismissMessage = viewModel::consumirMensaje
    )
}
