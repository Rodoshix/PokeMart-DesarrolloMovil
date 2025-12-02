package com.pokermart.ecommerce.ui.products

import com.pokermart.ecommerce.MainDispatcherRule
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import com.pokermart.ecommerce.data.repository.RepositorioCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `productos cargan y estado no cargando`() = runTest {
        val repo = RepositorioCatalogo(
            categoriaDao = object : CategoriaDao {
                override fun observarCategorias() = MutableStateFlow(emptyList<CategoriaEntity>())
                override suspend fun contar(): Int = 0
                override suspend fun insertarTodas(categorias: List<CategoriaEntity>) {}
            },
            productoDao = object : ProductoDao {
                override fun observarDestacados() = MutableStateFlow(emptyList<ProductoConOpciones>())
                override fun buscarPorTexto(query: String) = MutableStateFlow(emptyList<ProductoConOpciones>())
                override fun observarPorCategoria(categoriaId: Long) = MutableStateFlow(
                    listOf(
                        ProductoConOpciones(
                            producto = ProductoEntity(
                                id = 1,
                                categoriaId = categoriaId,
                                nombre = "P1",
                                descripcion = "",
                                precio = 1000.0,
                                imagenUrl = "",
                                destacado = false
                            ),
                            opciones = emptyList()
                        )
                    )
                )

                override suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones? = null
                override suspend fun contar(): Int = 0
                override suspend fun insertarTodos(productos: List<ProductoEntity>) {}
            }
        )
        val vm = ProductsViewModel(repo, categoriaId = 1, categoriaNombre = "Cat")
        val estado = vm.estado.value
        assertEquals(false, estado.cargando)
        assertEquals(1, estado.productos.size)
    }
}
