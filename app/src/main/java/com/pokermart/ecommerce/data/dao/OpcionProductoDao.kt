package com.pokermart.ecommerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity

@Dao
interface OpcionProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(opciones: List<OpcionProductoEntity>)

    @Query("SELECT COUNT(*) FROM opciones_producto")
    suspend fun contar(): Int
}
