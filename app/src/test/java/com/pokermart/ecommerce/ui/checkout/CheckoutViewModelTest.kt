package com.pokermart.ecommerce.ui.checkout

import com.google.android.gms.maps.model.LatLng
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class CheckoutViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun viewModel(
        direcciones: List<Direccion> = emptyList(),
        carrito: List<CarritoItem> = emptyList()
    ): CheckoutViewModel {
        val repoDirecciones = fakeDireccionesRepo(direcciones)
        val repoCarrito = fakeCarritoRepo(carrito)
        val repoCatalogo = fakeCatalogoRepo()
        val session = mockSession()
        return CheckoutViewModel(
            repositorioCarrito = repoCarrito,
            repositorioCatalogo = repoCatalogo,
            repositorioDirecciones = repoDirecciones,
            sessionManager = session
        )
    }

    @Test
    fun `retirar en tienda fija destino en mall`() = runTest {
        val vm = viewModel()
        vm.seleccionarEntrega(MetodoEntrega.RETIRO_TIENDA)
        val estado = vm.estado.value
        assertEquals(-33.52164, estado.destinoLat ?: 0.0, 0.0)
        assertEquals(-70.59867, estado.destinoLon ?: 0.0, 0.0)
    }

    @Test
    fun `envio usa coords de direccion`() = runTest {
        val direccion = Direccion(
            id = 10,
            usuarioId = 1,
            direccion = "Test",
            latitud = 1.0,
            longitud = 2.0
        )
        val vm = viewModel(direcciones = listOf(direccion))
        vm.seleccionarEntrega(MetodoEntrega.ENVIO)
        vm.seleccionarDireccion(direccion.id, direccion.latitud, direccion.longitud)
        val estado = vm.estado.value
        assertEquals(1.0, estado.destinoLat ?: 0.0, 0.0)
        assertEquals(2.0, estado.destinoLon ?: 0.0, 0.0)
    }

    @Test
    fun `origen se guarda cuando se detecta`() = runTest {
        val vm = viewModel()
        vm.actualizarOrigen(LatLng(1.0, 2.0))
        val estado = vm.estado.value
        assertEquals(1.0, estado.origenLat ?: 0.0, 0.0)
        assertEquals(2.0, estado.origenLon ?: 0.0, 0.0)
    }

    private fun fakeCarritoRepo(items: List<CarritoItem>) = RepositorioCarrito(
        carritoDao = FakeCarritoDao(items)
    )

    private fun fakeDireccionesRepo(direcciones: List<Direccion>) = RepositorioDirecciones(
        direccionDao = FakeDireccionDao(direcciones)
    )

    private fun fakeCatalogoRepo() = RepositorioCatalogo(
        categoriaDao = FakeCategoriaDao(),
        productoDao = FakeProductoDao()
    )
}

// --- Fakes simples de DAOs ---

private class FakeCarritoDao(items: List<CarritoItem>) : CarritoDao {
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
    override fun observarCantidadTotal(usuarioId: Long): Flow<Int> = MutableStateFlow(flow.value.size)
}

private class FakeDireccionDao(direcciones: List<Direccion>) : DireccionDao {
    private val flow = MutableStateFlow(direcciones.map { it.aEntidad() })
    override fun observarPredeterminada(usuarioId: Long) = flow.map { list -> list.firstOrNull { it.isDefault } }
    override fun observarTodas(usuarioId: Long) = flow
    override suspend fun guardar(direccion: DireccionEntity): Long = direccion.id
    override suspend fun establecerPredeterminada(id: Long) {}
    override suspend fun limpiarPredeterminadas(usuarioId: Long) {}
    override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
    override suspend fun contarTotal(): Int = flow.value.size
    override suspend fun contarPredeterminadas(usuarioId: Long): Int = 0
    override suspend fun obtenerPorId(id: Long): DireccionEntity? = flow.value.firstOrNull { it.id == id }
    override suspend fun obtenerMasReciente(usuarioId: Long): DireccionEntity? = flow.value.lastOrNull()
    override suspend fun eliminar(id: Long) {}
}

private class FakeCategoriaDao : CategoriaDao {
    override fun observarCategorias(): Flow<List<CategoriaEntity>> = MutableStateFlow(emptyList())
    override suspend fun contar(): Int = 0
    override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {}
}

private class FakeProductoDao : ProductoDao {
    override fun observarDestacados(): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
    override fun buscarPorTexto(query: String): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
    override fun observarPorCategoria(categoriaId: Long): Flow<List<ProductoConOpciones>> = MutableStateFlow(emptyList())
    override suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones? {
        val producto = ProductoEntity(
            id = productoId,
            categoriaId = 1,
            nombre = "Producto",
            descripcion = "desc",
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

private fun OpcionProducto.aEntidad() = OpcionProductoEntity(
    id = id,
    productoId = productoId,
    nombre = nombre,
    descripcion = descripcion,
    precioExtra = precioExtra,
    stock = stock
)

private fun Direccion.aEntidad() = DireccionEntity(
    id = id,
    usuarioId = usuarioId,
    etiqueta = etiqueta,
    addressLine = direccion,
    referencia = referencia,
    latitud = latitud,
    longitud = longitud,
    isDefault = esPredeterminada,
    createdAt = creadoEl
)

private fun mockSession(): SessionManager =
    Mockito.mock(SessionManager::class.java).apply {
        Mockito.`when`(obtenerSesion()).thenReturn(Usuario(id = 1, nombre = "Test", correo = "a@b.com"))
    }
