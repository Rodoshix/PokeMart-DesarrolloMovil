package com.pokermart.ecommerce.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.di.DI
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.ui.categories.CategoriesScreen
import com.pokermart.ecommerce.ui.categories.CategoriesViewModel
import com.pokermart.ecommerce.ui.home.HomeRoute
import com.pokermart.ecommerce.ui.home.HomeViewModel
import com.pokermart.ecommerce.ui.login.LoginScreen
import com.pokermart.ecommerce.ui.login.LoginViewModel
import com.pokermart.ecommerce.ui.products.ProductListScreen
import com.pokermart.ecommerce.ui.products.ProductOptionsScreen
import com.pokermart.ecommerce.ui.products.ARG_CATEGORIA_ID
import com.pokermart.ecommerce.ui.products.ARG_CATEGORIA_NOMBRE
import com.pokermart.ecommerce.ui.products.ARG_PRODUCTO_ID
import com.pokermart.ecommerce.ui.products.ProductOptionsViewModel
import com.pokermart.ecommerce.ui.products.ProductsViewModel
import com.pokermart.ecommerce.ui.profile.ProfileScreen
import com.pokermart.ecommerce.ui.profile.ProfileViewModel

private const val ARG_USUARIO_ID = "usuarioId"

sealed class Destino(val ruta: String) {
    data object Login : Destino("login")
    data object Home : Destino("home")
    data object Categorias : Destino("categorias")
    data object Productos : Destino("productos/{$ARG_CATEGORIA_ID}/{$ARG_CATEGORIA_NOMBRE}") {
        fun crearRuta(categoriaId: Long, categoriaNombre: String): String =
            "productos/$categoriaId/${Uri.encode(categoriaNombre)}"
    }

    data object OpcionesProducto : Destino("opciones/{$ARG_PRODUCTO_ID}") {
        fun crearRuta(productoId: Long): String = "opciones/$productoId"
    }

    data object Perfil : Destino("perfil/{$ARG_USUARIO_ID}") {
        fun crearRuta(usuarioId: Long): String = "perfil/$usuarioId"
    }
}

@Composable
fun NavGraph(
    sessionManager: SessionManager = DI.obtenerGestorSesion()
) {
    val navController = rememberNavController()
    val sesion = remember { sessionManager.obtenerSesion() }
    val destinoInicial = if (sesion == null) {
        Destino.Login.ruta
    } else {
        Destino.Home.ruta
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        composable(route = Destino.Login.ruta) {
            val loginViewModel = viewModel<LoginViewModel>(
                factory = DI.loginViewModelFactory()
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = { _: Usuario ->
                    navController.navigate(Destino.Home.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Destino.Home.ruta) {
            val homeViewModel = viewModel<HomeViewModel>(
                factory = DI.homeViewModelFactory()
            )
            HomeRoute(
                viewModel = homeViewModel,
                onChangeAddress = {
                    // Navega a la pantalla de direccion cuando exista
                },
                onCategoryClick = {
                    navController.navigate(Destino.Categorias.ruta)
                },
                onProductClick = { producto ->
                    navController.navigate(
                        Destino.OpcionesProducto.crearRuta(producto.id)
                    )
                }
            )
        }

        composable(route = Destino.Categorias.ruta) {
            val categoriesViewModel = viewModel<CategoriesViewModel>(
                factory = DI.categoriesViewModelFactory()
            )
            CategoriesScreen(
                viewModel = categoriesViewModel,
                onCategoriaSeleccionada = { categoria ->
                    navController.navigate(
                        Destino.Productos.crearRuta(
                            categoriaId = categoria.id,
                            categoriaNombre = categoria.nombre
                        )
                    )
                },
                onPerfilClick = {
                    val usuarioId = sessionManager.obtenerSesion()?.id
                    if (usuarioId != null) {
                        navController.navigate(Destino.Perfil.crearRuta(usuarioId))
                    }
                }
            )
        }

        composable(
            route = Destino.Productos.ruta,
            arguments = listOf(
                navArgument(ARG_CATEGORIA_ID) { type = NavType.LongType },
                navArgument(ARG_CATEGORIA_NOMBRE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoriaId = backStackEntry.arguments?.getLong(ARG_CATEGORIA_ID) ?: 0L
            val categoriaNombre = backStackEntry.arguments?.getString(ARG_CATEGORIA_NOMBRE) ?: "Productos"
            val productsViewModel = viewModel<ProductsViewModel>(
                viewModelStoreOwner = backStackEntry,
                factory = DI.productsViewModelFactory(
                    categoriaId = categoriaId,
                    categoriaNombre = categoriaNombre
                )
            )
            ProductListScreen(
                viewModel = productsViewModel,
                onProductoSeleccionado = { producto ->
                    navController.navigate(
                        Destino.OpcionesProducto.crearRuta(producto.id)
                    )
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destino.OpcionesProducto.ruta,
            arguments = listOf(
                navArgument(ARG_PRODUCTO_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getLong(ARG_PRODUCTO_ID) ?: 0L
            val opcionesViewModel = viewModel<ProductOptionsViewModel>(
                viewModelStoreOwner = backStackEntry,
                factory = DI.productOptionsViewModelFactory(productoId)
            )
            ProductOptionsScreen(
                viewModel = opcionesViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destino.Perfil.ruta,
            arguments = listOf(
                navArgument(ARG_USUARIO_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getLong(ARG_USUARIO_ID) ?: 0L
            val profileViewModel = viewModel<ProfileViewModel>(
                viewModelStoreOwner = backStackEntry,
                factory = DI.profileViewModelFactory()
            )
            ProfileScreen(
                viewModel = profileViewModel,
                usuarioId = usuarioId,
                onCerrarSesion = {
                    navController.navigate(Destino.Login.ruta) {
                        popUpTo(0)
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
