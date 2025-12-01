package com.pokermart.ecommerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokermart.ecommerce.data.database.entities.CarritoItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito WHERE usuario_id = :usuarioId ORDER BY agregado_el DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<CarritoItemEntity>>

    @Query(
        """
        SELECT * FROM carrito 
        WHERE usuario_id = :usuarioId 
          AND producto_id = :productoId 
          AND ((opcion_id IS NULL AND :opcionId IS NULL) OR opcion_id = :opcionId)
        LIMIT 1
        """
    )
    suspend fun obtenerItem(
        usuarioId: Long,
        productoId: Long,
        opcionId: Long?
    ): CarritoItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItemEntity): Long

    @Update
    suspend fun actualizar(item: CarritoItemEntity)

    @Query("UPDATE carrito SET cantidad = :cantidad WHERE id = :id")
    suspend fun actualizarCantidad(id: Long, cantidad: Int)

    @Query("DELETE FROM carrito WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("DELETE FROM carrito WHERE usuario_id = :usuarioId")
    suspend fun limpiarPorUsuario(usuarioId: Long)

    @Query("SELECT COUNT(*) FROM carrito WHERE usuario_id = :usuarioId")
    suspend fun contarPorUsuario(usuarioId: Long): Int

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM carrito WHERE usuario_id = :usuarioId")
    fun observarCantidadTotal(usuarioId: Long): Flow<Int>
}
