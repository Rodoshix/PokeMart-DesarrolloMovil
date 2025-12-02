package com.pokermart.ecommerce.ui.home

import com.pokermart.ecommerce.MainDispatcherRule
import com.pokermart.ecommerce.data.dao.CarritoDao
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.database.entities.CarritoItemEntity
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun vm(
        productos: List<Producto> = emptyList(),
        direccion: String? = null,
        cartCount: Int = 0,
        hasSession: Boolean = true
    ): HomeViewModel {
        return HomeViewModel(
            sessionManager = mockSession(hasSession),
            repositorioCatalogo = fakeCatalogoRepo(productos),
            repositorioDirecciones = fakeDireccionesRepo(direccion),
            repositorioCarrito = fakeCarritoRepo(cartCount)
        )
    }

    @Test
    fun `busqueda actualiza query en estado`() = runTest {
        val vm = vm()
        vm.onSearchChange("Pocion")
        assertEquals("Pocion", vm.uiState.searchQuery)
    }

    @Test
    fun `direccion predeterminada se refleja en ui`() = runTest {
        val vm = vm(direccion = "Mi casa")
        assertEquals("Mi casa", vm.uiState.address)
    }

    @Test
    fun `contador de carrito se refleja en ui`() = runTest {
        val vm = vm(cartCount = 3, hasSession = true)
        advanceUntilIdle()
        assertEquals(3, vm.uiState.cartCount)
    }
}

// Fakes

private fun fakeCatalogoRepo(productos: List<Producto>) = RepositorioCatalogo(
    categoriaDao = object : CategoriaDao {
        override fun observarCategorias(): Flow<List<CategoriaEntity>> = MutableStateFlow(emptyList())
        override suspend fun contar(): Int = 0
        override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {}
    },
    productoDao = object : ProductoDao {
        private val flow = MutableStateFlow(
            productos.map {
                ProductoConOpciones(
                    producto = ProductoEntity(
                        id = it.id,
                        categoriaId = it.categoriaId,
                        nombre = it.nombre,
                        descripcion = it.descripcion,
                        precio = it.precio,
                        imagenUrl = it.imagenUrl,
                        destacado = it.destacado
                    ),
                    opciones = emptyList()
                )
            }
        )
        override fun observarDestacados(): Flow<List<ProductoConOpciones>> = flow
        override fun buscarPorTexto(query: String): Flow<List<ProductoConOpciones>> = flow
        override fun observarPorCategoria(categoriaId: Long): Flow<List<ProductoConOpciones>> = flow
        override suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones? = flow.value.firstOrNull()
        override suspend fun contar(): Int = flow.value.size
        override suspend fun insertarTodos(productos: List<ProductoEntity>) {}
    }
)

private fun fakeDireccionesRepo(direccion: String?) = RepositorioDirecciones(
    direccionDao = object : DireccionDao {
        private val pred = DireccionEntity(
            id = 1,
            usuarioId = 1,
            etiqueta = "Pred",
            addressLine = direccion ?: "",
            referencia = null,
            latitud = null,
            longitud = null,
            isDefault = true,
            createdAt = System.currentTimeMillis()
        )
        private val flow = MutableStateFlow(if (direccion != null) listOf(pred) else emptyList())
        override fun observarPredeterminada(usuarioId: Long): Flow<DireccionEntity?> = flow.map { it.firstOrNull() }
        override fun observarTodas(usuarioId: Long): Flow<List<DireccionEntity>> = flow
        override suspend fun guardar(direccion: DireccionEntity): Long = direccion.id
        override suspend fun establecerPredeterminada(id: Long) {
            flow.value = flow.value.map { it.copy(isDefault = it.id == id) }
        }
        override suspend fun limpiarPredeterminadas(usuarioId: Long) {
            flow.value = flow.value.map { it.copy(isDefault = false) }
        }
        override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
        override suspend fun contarPredeterminadas(usuarioId: Long): Int = flow.value.size
        override suspend fun contarTotal(): Int = flow.value.size
        override suspend fun obtenerPorId(id: Long): DireccionEntity? = flow.value.firstOrNull { it.id == id }
        override suspend fun obtenerMasReciente(usuarioId: Long): DireccionEntity? = flow.value.lastOrNull()
        override suspend fun eliminar(id: Long) {}
    }
)

private fun fakeCarritoRepo(count: Int) = RepositorioCarrito(
    carritoDao = object : CarritoDao {
        private val flow = MutableStateFlow(List(count) { CarritoItemEntity(usuarioId = 1, productoId = it.toLong(), opcionId = null, cantidad = 1, precioUnitario = 1000.0) })
        override fun observarPorUsuario(usuarioId: Long): Flow<List<CarritoItemEntity>> = flow
        override suspend fun obtenerItem(usuarioId: Long, productoId: Long, opcionId: Long?): CarritoItemEntity? = null
        override suspend fun insertar(item: CarritoItemEntity): Long = 0
        override suspend fun actualizar(item: CarritoItemEntity) {}
        override suspend fun actualizarCantidad(id: Long, cantidad: Int) {}
        override suspend fun eliminarPorId(id: Long) {}
        override suspend fun limpiarPorUsuario(usuarioId: Long) {}
        override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
        override fun observarCantidadTotal(usuarioId: Long): Flow<Int> = flow.map { list -> list.sumOf { it.cantidad } }
    }
)

private fun mockSession(hasSession: Boolean): SessionManager =
    Mockito.mock(SessionManager::class.java).apply {
        val user = if (hasSession) Usuario(id = 1, nombre = "Test", correo = "a@b.com") else null
        Mockito.`when`(obtenerSesion()).thenReturn(user)
    }
