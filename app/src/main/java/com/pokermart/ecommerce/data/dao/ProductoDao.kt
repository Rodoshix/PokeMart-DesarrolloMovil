package com.pokermart.ecommerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.relaciones.ProductoConOpciones
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Transaction
    @Query("SELECT * FROM productos WHERE categoria_id = :categoriaId ORDER BY nombre")
    fun observarPorCategoria(categoriaId: Long): Flow<List<ProductoConOpciones>>

    @Transaction
    @Query("SELECT * FROM productos WHERE destacado = 1 ORDER BY id DESC LIMIT 6")
    fun observarDestacados(): Flow<List<ProductoConOpciones>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    @Transaction
    @Query("SELECT * FROM productos WHERE id = :productoId LIMIT 1")
    suspend fun obtenerConOpciones(productoId: Long): ProductoConOpciones?
}
