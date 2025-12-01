package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Pedido

@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DireccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["direccion_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("usuario_id"),
        Index("direccion_id")
    ]
)
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "usuario_id") val usuarioId: Long,
    @ColumnInfo(name = "direccion_id") val direccionId: Long?,
    val subtotal: Double,
    val impuesto: Double,
    val envio: Double,
    val servicio: Double,
    val total: Double,
    @ColumnInfo(name = "metodo_pago") val metodoPago: String,
    @ColumnInfo(name = "metodo_entrega") val metodoEntrega: String,
    @ColumnInfo(name = "creado_el") val creadoEl: Long = System.currentTimeMillis()
)

fun PedidoEntity.aModelo(items: List<PedidoItemEntity>): Pedido = Pedido(
    id = id,
    usuarioId = usuarioId,
    direccionId = direccionId,
    subtotal = subtotal,
    impuesto = impuesto,
    envio = envio,
    servicio = servicio,
    total = total,
    metodoPago = metodoPago,
    metodoEntrega = metodoEntrega,
    creadoEl = creadoEl,
    items = items.map { it.aModelo() }
)

fun Pedido.aEntidad(): PedidoEntity = PedidoEntity(
    id = id,
    usuarioId = usuarioId,
    direccionId = direccionId,
    subtotal = subtotal,
    impuesto = impuesto,
    envio = envio,
    servicio = servicio,
    total = total,
    metodoPago = metodoPago,
    metodoEntrega = metodoEntrega,
    creadoEl = creadoEl
)
