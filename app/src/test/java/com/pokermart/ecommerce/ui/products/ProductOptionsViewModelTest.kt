package com.pokermart.ecommerce.ui.products

import com.pokermart.ecommerce.MainDispatcherRule
import com.pokermart.ecommerce.data.dao.CarritoDao
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.database.entities.CarritoItemEntity
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Producto
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.data.repository.RepositorioCarrito
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class ProductOptionsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun vm(hasSession: Boolean = true): ProductOptionsViewModel {
        return ProductOptionsViewModel(
            repositorioCatalogo = fakeCatalogoRepo(),
            repositorioCarrito = fakeCarritoRepo(),
            sessionManager = mockSession(hasSession),
            productoId = 1
        )
    }
}

// Fakes
private fun fakeCatalogoRepo() = RepositorioCatalogo(
    categoriaDao = FakeCatDao(),
    productoDao = object : ProductoDao {
        override fun observarDestacados(): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
        override fun buscarPorTexto(query: String): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
        override fun observarPorCategoria(categoriaId: Long): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
        override suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones? {
            val producto = ProductoEntity(
                id = productoId,
                categoriaId = 1,
                nombre = "Test",
                descripcion = "",
                precio = 1000.0,
                imagenUrl = "",
                destacado = false
            )
            val opcion = OpcionProducto(
                id = 1,
                productoId = productoId,
                nombre = "x1",
                descripcion = "",
                precioExtra = 0.0,
                stock = 10
            ).aEntidad()
            return ProductoConOpciones(producto, listOf(opcion))
        }

        override suspend fun contar(): Int = 0
        override suspend fun insertarTodos(productos: List<ProductoEntity>) {}
    }
)

private fun fakeCarritoRepo() = RepositorioCarrito(
    carritoDao = object : CarritoDao {
        override fun observarPorUsuario(usuarioId: Long): Flow<List<CarritoItemEntity>> = MutableStateFlow(emptyList())
        override suspend fun obtenerItem(usuarioId: Long, productoId: Long, opcionId: Long?): CarritoItemEntity? = null
        override suspend fun insertar(item: CarritoItemEntity): Long = 1
        override suspend fun actualizar(item: CarritoItemEntity) {}
        override suspend fun actualizarCantidad(id: Long, cantidad: Int) {}
        override suspend fun eliminarPorId(id: Long) {}
        override suspend fun limpiarPorUsuario(usuarioId: Long) {}
        override suspend fun contarPorUsuario(usuarioId: Long): Int = 0
        override fun observarCantidadTotal(usuarioId: Long): Flow<Int> = MutableStateFlow(0)
    }
)

private class FakeCatDao : CategoriaDao {
    override fun observarCategorias(): Flow<List<CategoriaEntity>> = MutableStateFlow(emptyList())
    override suspend fun contar(): Int = 0
    override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {}
}

private fun OpcionProducto.aEntidad() = OpcionProductoEntity(
    id = id,
    productoId = productoId,
    nombre = nombre,
    descripcion = descripcion,
    precioExtra = precioExtra,
    stock = stock
)

private fun mockSession(hasSession: Boolean): SessionManager =
    Mockito.mock(SessionManager::class.java).apply {
        val user = if (hasSession) Usuario(id = 1, nombre = "Test", correo = "a@b.com") else null
        Mockito.`when`(obtenerSesion()).thenReturn(user)
    }
