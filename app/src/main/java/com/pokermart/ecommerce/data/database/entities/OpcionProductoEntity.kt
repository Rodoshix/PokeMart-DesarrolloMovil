package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.OpcionProducto

@Entity(
    tableName = "opciones_producto",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("producto_id")]
)
data class OpcionProductoEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "producto_id") val productoId: Long,
    val nombre: String,
    val descripcion: String,
    @ColumnInfo(name = "precio_extra") val precioExtra: Double,
    val stock: Int
)

fun OpcionProductoEntity.aModelo() = OpcionProducto(
    id = id,
    productoId = productoId,
    nombre = nombre,
    descripcion = descripcion,
    precioExtra = precioExtra,
    stock = stock
)

fun OpcionProducto.aEntity() = OpcionProductoEntity(
    id = id,
    productoId = productoId,
    nombre = nombre,
    descripcion = descripcion,
    precioExtra = precioExtra,
    stock = stock
)
