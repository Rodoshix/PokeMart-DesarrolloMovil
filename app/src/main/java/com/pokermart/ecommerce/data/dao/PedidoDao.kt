package com.pokermart.ecommerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.pokermart.ecommerce.data.database.entities.PedidoEntity
import com.pokermart.ecommerce.data.database.entities.PedidoItemEntity
import com.pokermart.ecommerce.data.database.entities.relaciones.PedidoConItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert
    suspend fun insertarPedido(pedido: PedidoEntity): Long

    @Insert
    suspend fun insertarItems(items: List<PedidoItemEntity>)

    @Transaction
    @Query("SELECT * FROM pedidos WHERE usuario_id = :usuarioId ORDER BY creado_el DESC")
    fun observarPedidos(usuarioId: Long): Flow<List<PedidoConItems>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId LIMIT 1")
    suspend fun obtenerDetalle(pedidoId: Long): PedidoConItems?
}
