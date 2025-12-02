package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.CarritoDao
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.model.CarritoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RepositorioCarrito(
    private val carritoDao: CarritoDao
    // Las referencias a otros repositorios se agregaran cuando necesitemos enriquecer datos.
) {

    @Suppress("unused")
    fun observarCarrito(usuarioId: Long): Flow<List<CarritoItem>> =
        carritoDao.observarPorUsuario(usuarioId).map { entidades ->
            entidades.map { it.aModelo() }
        }

    suspend fun agregarOIncrementar(
        usuarioId: Long,
        productoId: Long,
        opcionId: Long?,
        precioUnitario: Double
    ) = withContext(Dispatchers.IO) {
        val existente = carritoDao.obtenerItem(
            usuarioId = usuarioId,
            productoId = productoId,
            opcionId = opcionId
        )
        if (existente != null) {
            val nuevaCantidad = (existente.cantidad + 1).coerceAtMost(Int.MAX_VALUE)
            carritoDao.actualizarCantidad(existente.id, nuevaCantidad)
        } else {
            val nuevo = CarritoItem(
                usuarioId = usuarioId,
                productoId = productoId,
                opcionId = opcionId,
                cantidad = 1,
                precioUnitario = precioUnitario
            )
            carritoDao.insertar(nuevo.aEntidad())
        }
    }

    suspend fun actualizarCantidad(itemId: Long, cantidad: Int) = withContext(Dispatchers.IO) {
        val cantidadSegura = cantidad.coerceAtLeast(1)
        carritoDao.actualizarCantidad(itemId, cantidadSegura)
    }

    suspend fun eliminarItem(itemId: Long) = withContext(Dispatchers.IO) {
        carritoDao.eliminarPorId(itemId)
    }

    suspend fun limpiar(usuarioId: Long) = withContext(Dispatchers.IO) {
        carritoDao.limpiarPorUsuario(usuarioId)
    }

    @Suppress("unused")
    suspend fun contarItems(usuarioId: Long): Int = withContext(Dispatchers.IO) {
        carritoDao.contarPorUsuario(usuarioId)
    }

    fun observarCantidadTotal(usuarioId: Long): Flow<Int> =
        carritoDao.observarCantidadTotal(usuarioId)
}
