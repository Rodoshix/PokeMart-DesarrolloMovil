package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.PedidoDao
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.database.entities.relaciones.aModelo
import com.pokermart.ecommerce.data.model.Pedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RepositorioPedidos(
    private val pedidoDao: PedidoDao
) {

    fun observarPedidos(usuarioId: Long): Flow<List<Pedido>> =
        pedidoDao.observarPedidos(usuarioId).map { lista -> lista.map { it.aModelo() } }

    suspend fun obtenerDetalle(pedidoId: Long): Pedido? = withContext(Dispatchers.IO) {
        pedidoDao.obtenerDetalle(pedidoId)?.aModelo()
    }

    suspend fun crearPedido(pedido: Pedido): Pedido = withContext(Dispatchers.IO) {
        val pedidoId = pedidoDao.insertarPedido(pedido.aEntidad())
        val items = pedido.items.map { item ->
            val conPedidoId = if (item.pedidoId == 0L) item.copy(pedidoId = pedidoId) else item
            conPedidoId.aEntidad()
        }
        if (items.isNotEmpty()) {
            pedidoDao.insertarItems(items)
        }
        val guardado = pedidoDao.obtenerDetalle(pedidoId)
        guardado?.aModelo() ?: pedido.copy(id = pedidoId)
    }
}
