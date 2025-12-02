package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.PedidoItem

@Entity(
    tableName = "pedido_items",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedido_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["producto_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = OpcionProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["opcion_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("pedido_id"),
        Index("producto_id"),
        Index("opcion_id")
    ]
)
data class PedidoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "pedido_id") val pedidoId: Long,
    @ColumnInfo(name = "producto_id") val productoId: Long,
    @ColumnInfo(name = "opcion_id") val opcionId: Long?,
    val cantidad: Int,
    @ColumnInfo(name = "precio_unitario") val precioUnitario: Double,
    @ColumnInfo(name = "precio_total") val precioTotal: Double
)

fun PedidoItemEntity.aModelo() = PedidoItem(
    id = id,
    pedidoId = pedidoId,
    productoId = productoId,
    opcionId = opcionId,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    precioTotal = precioTotal
)

fun PedidoItem.aEntidad() = PedidoItemEntity(
    id = id,
    pedidoId = pedidoId,
    productoId = productoId,
    opcionId = opcionId,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    precioTotal = precioTotal
)
