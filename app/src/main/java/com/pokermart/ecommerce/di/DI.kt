package com.pokermart.ecommerce.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokermart.ecommerce.data.database.PokeMartDatabase
import com.pokermart.ecommerce.data.repository.RepositorioAutenticacion
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.ui.categories.CategoriesViewModel
import com.pokermart.ecommerce.ui.login.LoginViewModel
import com.pokermart.ecommerce.ui.products.ProductOptionsViewModel
import com.pokermart.ecommerce.ui.products.ProductsViewModel
import com.pokermart.ecommerce.ui.profile.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DI {

    private lateinit var aplicacion: Application
    private lateinit var baseDeDatos: PokeMartDatabase
    private lateinit var gestorSesion: SessionManager

    private val alcanceAplicacion = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repositorioAutenticacion: RepositorioAutenticacion by lazy {
        RepositorioAutenticacion(baseDeDatos.usuarioDao())
    }
    private val repositorioCatalogo: RepositorioCatalogo by lazy {
        RepositorioCatalogo(
            categoriaDao = baseDeDatos.categoriaDao(),
            productoDao = baseDeDatos.productoDao()
        )
    }

    fun inicializar(app: Application) {
        if (this::aplicacion.isInitialized) return
        aplicacion = app
        baseDeDatos = PokeMartDatabase.obtenerInstancia(app, alcanceAplicacion)
        gestorSesion = SessionManager(app)
    }

    fun obtenerGestorSesion(): SessionManager = gestorSesion

    fun obtenerRepositorioAutenticacion(): RepositorioAutenticacion = repositorioAutenticacion

    fun obtenerRepositorioCatalogo(): RepositorioCatalogo = repositorioCatalogo

    internal fun alcanceGlobal(): CoroutineScope = alcanceAplicacion

    fun loginViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LoginViewModel(
                repositorioAutenticacion = obtenerRepositorioAutenticacion(),
                gestorSesion = obtenerGestorSesion()
            )
        }
    }

    fun categoriesViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            CategoriesViewModel(repositorioCatalogo = obtenerRepositorioCatalogo())
        }
    }

    fun productsViewModelFactory(
        categoriaId: Long,
        categoriaNombre: String
    ): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProductsViewModel(
                repositorioCatalogo = obtenerRepositorioCatalogo(),
                categoriaId = categoriaId,
                categoriaNombre = categoriaNombre
            )
        }
    }

    fun productOptionsViewModelFactory(productoId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProductOptionsViewModel(
                repositorioCatalogo = obtenerRepositorioCatalogo(),
                productoId = productoId
            )
        }
    }

    fun profileViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProfileViewModel(
                application = aplicacion,
                repositorioAutenticacion = obtenerRepositorioAutenticacion(),
                sessionManager = obtenerGestorSesion()
            )
        }
    }
}
