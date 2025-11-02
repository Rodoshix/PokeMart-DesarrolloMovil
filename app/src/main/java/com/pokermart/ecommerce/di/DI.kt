package com.pokermart.ecommerce.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokermart.ecommerce.data.database.PokeMartDatabase
import com.pokermart.ecommerce.data.repository.RepositorioAutenticacion
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.ui.home.HomeViewModel
import com.pokermart.ecommerce.ui.address.EnviarAViewModel
import com.pokermart.ecommerce.ui.login.LoginViewModel
import com.pokermart.ecommerce.ui.products.ProductOptionsViewModel
import com.pokermart.ecommerce.ui.products.ProductsViewModel
import com.pokermart.ecommerce.ui.profile.ProfileViewModel
import com.pokermart.ecommerce.ui.register.RegisterViewModel
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
    private val repositorioDirecciones: RepositorioDirecciones by lazy {
        RepositorioDirecciones(baseDeDatos.direccionDao())
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
    fun obtenerRepositorioDirecciones(): RepositorioDirecciones = repositorioDirecciones

    internal fun alcanceGlobal(): CoroutineScope = alcanceAplicacion

    fun loginViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LoginViewModel(
                repositorioAutenticacion = obtenerRepositorioAutenticacion(),
                gestorSesion = obtenerGestorSesion()
            )
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

    fun registerViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            RegisterViewModel(
                repositorioAutenticacion = obtenerRepositorioAutenticacion(),
                sessionManager = obtenerGestorSesion()
            )
        }
    }

    fun profileViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProfileViewModel(
                application = aplicacion,
                repositorioAutenticacion = obtenerRepositorioAutenticacion(),
                repositorioDirecciones = obtenerRepositorioDirecciones(),
                sessionManager = obtenerGestorSesion()
            )
        }
    }

    fun enviarAViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            EnviarAViewModel(
                repositorioDirecciones = obtenerRepositorioDirecciones(),
                sessionManager = obtenerGestorSesion()
            )
        }
    }

    fun homeViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                sessionManager = obtenerGestorSesion(),
                repositorioCatalogo = obtenerRepositorioCatalogo(),
                repositorioDirecciones = obtenerRepositorioDirecciones()
            )
        }
    }
}
