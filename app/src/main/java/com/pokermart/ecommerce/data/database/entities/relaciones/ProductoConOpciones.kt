package com.pokermart.ecommerce.data.database.entities.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity

data class ProductoConOpciones(
    @Embedded val producto: ProductoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "producto_id"
    )
    val opciones: List<OpcionProductoEntity>
)
