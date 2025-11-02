package com.pokermart.ecommerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DireccionDao {

    @Query("SELECT * FROM direccion WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): DireccionEntity?

    @Query("SELECT * FROM direccion WHERE usuario_id = :usuarioId AND is_default = 1 LIMIT 1")
    fun observarPredeterminada(usuarioId: Long): Flow<DireccionEntity?>

    @Query("SELECT * FROM direccion WHERE usuario_id = :usuarioId ORDER BY is_default DESC, id DESC")
    fun observarTodas(usuarioId: Long): Flow<List<DireccionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(direccion: DireccionEntity): Long

    @Query("UPDATE direccion SET is_default = 0 WHERE usuario_id = :usuarioId")
    suspend fun limpiarPredeterminadas(usuarioId: Long)

    @Query("UPDATE direccion SET is_default = 1 WHERE id = :direccionId")
    suspend fun establecerPredeterminada(direccionId: Long)

    @Query("SELECT COUNT(*) FROM direccion WHERE usuario_id = :usuarioId")
    suspend fun contarPorUsuario(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM direccion WHERE usuario_id = :usuarioId AND is_default = 1")
    suspend fun contarPredeterminadas(usuarioId: Long): Int

    @Query("SELECT COUNT(*) FROM direccion")
    suspend fun contarTotal(): Int

    @Query("DELETE FROM direccion WHERE id = :direccionId")
    suspend fun eliminar(direccionId: Long)

    @Query("SELECT * FROM direccion WHERE usuario_id = :usuarioId ORDER BY created_at DESC LIMIT 1")
    suspend fun obtenerMasReciente(usuarioId: Long): DireccionEntity?
}
