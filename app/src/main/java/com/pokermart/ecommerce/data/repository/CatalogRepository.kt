package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import com.pokermart.ecommerce.data.model.Categoria
import com.pokermart.ecommerce.data.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RepositorioCatalogo(
    private val categoriaDao: CategoriaDao,
    private val productoDao: ProductoDao
) {

    fun observarCategorias(): Flow<List<Categoria>> =
        categoriaDao.observarCategorias().map { entidades ->
            entidades.map { it.aModelo() }
        }

    fun observarProductosPorCategoria(categoriaId: Long): Flow<List<Producto>> =
        productoDao.observarPorCategoria(categoriaId).map { relaciones ->
            relaciones.map { it.aModelo() }
        }

    suspend fun obtenerDetalleProducto(productoId: Long): Producto? =
        withContext(Dispatchers.IO) {
            productoDao.obtenerConOpciones(productoId)?.aModelo()
        }

    private fun ProductoConOpciones.aModelo(): Producto =
        producto.aModelo(opciones)
}
