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
import com.pokermart.ecommerce.ui.login.LoginScreen
import com.pokermart.ecommerce.ui.login.LoginViewModel
import com.pokermart.ecommerce.ui.products.ProductListScreen
import com.pokermart.ecommerce.ui.products.ProductOptionsScreen
import com.pokermart.ecommerce.ui.products.ARG_CATEGORIA_ID
import com.pokermart.ecommerce.ui.products.ARG_CATEGORIA_NOMBRE
import com.pokermart.ecommerce.ui.products.ARG_PRODUCTO_ID
import com.pokermart.ecommerce.ui.products.ProductOptionsViewModel
import com.pokermart.ecommerce.ui.products.ProductsViewModel

sealed class Destino(val ruta: String) {
    data object Login : Destino("login")
    data object Categorias : Destino("categorias")
    data object Productos : Destino("productos/{$ARG_CATEGORIA_ID}/{$ARG_CATEGORIA_NOMBRE}") {
        fun crearRuta(categoriaId: Long, categoriaNombre: String): String =
            "productos/$categoriaId/${Uri.encode(categoriaNombre)}"
    }

    data object OpcionesProducto : Destino("opciones/{$ARG_PRODUCTO_ID}") {
        fun crearRuta(productoId: Long): String = "opciones/$productoId"
    }
}

@Composable
fun AppNav(
    sessionManager: SessionManager = DI.obtenerGestorSesion()
) {
    val navController = rememberNavController()
    val sesion = remember { sessionManager.obtenerSesion() }
    val destinoInicial = if (sesion == null) {
        Destino.Login.ruta
    } else {
        Destino.Categorias.ruta
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
                    navController.navigate(Destino.Categorias.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                    }
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
    }
}
