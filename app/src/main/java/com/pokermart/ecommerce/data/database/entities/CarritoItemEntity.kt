package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.CarritoItem

@Entity(
    tableName = "carrito",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OpcionProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["opcion_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("usuario_id"),
        Index("producto_id"),
        Index("opcion_id"),
        Index(value = ["usuario_id", "producto_id", "opcion_id"], unique = true)
    ]
)
data class CarritoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "usuario_id") val usuarioId: Long,
    @ColumnInfo(name = "producto_id") val productoId: Long,
    @ColumnInfo(name = "opcion_id") val opcionId: Long?,
    val cantidad: Int,
    @ColumnInfo(name = "precio_unitario") val precioUnitario: Double,
    @ColumnInfo(name = "agregado_el") val agregadoEl: Long = System.currentTimeMillis()
)

fun CarritoItemEntity.aModelo() = CarritoItem(
    id = id,
    usuarioId = usuarioId,
    productoId = productoId,
    opcionId = opcionId,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    agregadoEl = agregadoEl
)

fun CarritoItem.aEntidad() = CarritoItemEntity(
    id = id,
    usuarioId = usuarioId,
    productoId = productoId,
    opcionId = opcionId,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    agregadoEl = agregadoEl
)
