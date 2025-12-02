package com.pokermart.ecommerce.data.database.entities.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.pokermart.ecommerce.data.database.entities.PedidoEntity
import com.pokermart.ecommerce.data.database.entities.PedidoItemEntity
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.model.Pedido

data class PedidoConItems(
    @Embedded val pedido: PedidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pedido_id"
    )
    val items: List<PedidoItemEntity>
)

fun PedidoConItems.aModelo(): Pedido = pedido.aModelo(items)
