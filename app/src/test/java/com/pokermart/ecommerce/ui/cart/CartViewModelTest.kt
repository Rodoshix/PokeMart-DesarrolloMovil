package com.pokermart.ecommerce.ui.cart

import com.pokermart.ecommerce.MainDispatcherRule
import com.pokermart.ecommerce.data.dao.CarritoDao
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.database.entities.CarritoItemEntity
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import com.pokermart.ecommerce.data.model.CarritoItem
import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun vm(
        carrito: List<CarritoItem> = emptyList(),
        direcciones: List<Direccion> = emptyList(),
        hasSession: Boolean = true
    ): CartViewModel {
        val precios = carrito.associate { it.productoId to it.precioUnitario }
        return CartViewModel(
            repositorioCarrito = fakeCarritoRepo(carrito),
            repositorioCatalogo = fakeCatalogoRepo(precios),
            repositorioDirecciones = fakeDireccionesRepo(direcciones),
            sessionManager = mockSession(hasSession)
        )
    }

    @Test
    fun `sin sesion muestra mensaje de error`() = runTest {
        val vm = vm(hasSession = false)
        advanceUntilIdle()
        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertTrue(estado.mensajeError?.contains("iniciar sesion") == true)
    }
}

// --- Fakes ---

private fun fakeCarritoRepo(items: List<CarritoItem>) = RepositorioCarrito(
    carritoDao = object : CarritoDao {
        private val flow = MutableStateFlow(items.map { it.aEntidad() })
        override fun observarPorUsuario(usuarioId: Long): Flow<List<CarritoItemEntity>> = flow
        override suspend fun obtenerItem(usuarioId: Long, productoId: Long, opcionId: Long?): CarritoItemEntity? = null
        override suspend fun insertar(item: CarritoItemEntity): Long = 0
        override suspend fun actualizar(item: CarritoItemEntity) {}
        override suspend fun actualizarCantidad(id: Long, cantidad: Int) {}
        override suspend fun eliminarPorId(id: Long) {}
        override suspend fun limpiarPorUsuario(usuarioId: Long) {
            flow.value = emptyList()
        }
        override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
        override fun observarCantidadTotal(usuarioId: Long): Flow<Int> = MutableStateFlow(flow.value.sumOf { it.cantidad })
    }
)

private fun fakeDireccionesRepo(direcciones: List<Direccion>) = RepositorioDirecciones(
    direccionDao = object : DireccionDao {
        private val flow = MutableStateFlow(direcciones.map { it.aEntidad() })
        override fun observarPredeterminada(usuarioId: Long): Flow<DireccionEntity?> =
            flow.map { list -> list.firstOrNull { it.isDefault } }
        override fun observarTodas(usuarioId: Long): Flow<List<DireccionEntity>> = flow
        override suspend fun guardar(direccion: DireccionEntity): Long = direccion.id
        override suspend fun establecerPredeterminada(id: Long) {
            flow.value = flow.value.map { it.copy(isDefault = it.id == id) }
        }
        override suspend fun limpiarPredeterminadas(usuarioId: Long) {
            flow.value = flow.value.map { it.copy(isDefault = false) }
        }
        override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
        override suspend fun contarTotal(): Int = flow.value.size
        override suspend fun contarPredeterminadas(usuarioId: Long): Int = 0
        override suspend fun obtenerPorId(id: Long): DireccionEntity? = flow.value.firstOrNull { it.id == id }
        override suspend fun obtenerMasReciente(usuarioId: Long): DireccionEntity? = flow.value.lastOrNull()
        override suspend fun eliminar(id: Long) {}
    }
)

private fun fakeCatalogoRepo(precios: Map<Long, Double>) = RepositorioCatalogo(
    categoriaDao = object : CategoriaDao {
        override fun observarCategorias() = MutableStateFlow(emptyList<CategoriaEntity>())
        override suspend fun contar(): Int = 0
        override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {}
    },
    productoDao = object : ProductoDao {
        override fun observarDestacados() = MutableStateFlow(emptyList<ProductoConOpciones>())
        override fun buscarPorTexto(query: String) = MutableStateFlow(emptyList<ProductoConOpciones>())
        override fun observarPorCategoria(categoriaId: Long) = MutableStateFlow(emptyList<ProductoConOpciones>())
        override suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones? =
            ProductoConOpciones(
                producto = ProductoEntity(
                    id = productoId,
                    categoriaId = 1,
                    nombre = "Producto",
                    descripcion = "",
                    precio = precios[productoId] ?: 0.0,
                    imagenUrl = "",
                    destacado = false
                ),
                opciones = listOf(
                    OpcionProducto(
                        id = 1,
                        productoId = productoId,
                        nombre = "x1",
                        descripcion = "",
                        precioExtra = 0.0,
                        stock = 10
                    ).toEntity()
                )
            )
        override suspend fun contar(): Int = 0
        override suspend fun insertarTodos(productos: List<ProductoEntity>) {}
    }
)

private fun mockSession(hasSession: Boolean): SessionManager =
    Mockito.mock(SessionManager::class.java).apply {
        val user = if (hasSession) Usuario(
            id = 1,
            nombre = "Test",
            apellido = "User",
            correo = "a@b.com",
            run = "11.111.111-1",
            fechaNacimiento = "1990-01-01"
        ) else null
        Mockito.`when`(obtenerSesion()).thenReturn(user)
    }

private fun OpcionProducto.toEntity() = OpcionProductoEntity(
    id = id,
    productoId = productoId,
    nombre = nombre,
    descripcion = descripcion,
    precioExtra = precioExtra,
    stock = stock
)
