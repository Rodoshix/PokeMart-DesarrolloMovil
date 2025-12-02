package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Producto

@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoria_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoria_id")]
)
data class ProductoEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "categoria_id") val categoriaId: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String,
    val destacado: Boolean
)

fun ProductoEntity.aModelo(opciones: List<OpcionProductoEntity>) = Producto(
    id = id,
    categoriaId = categoriaId,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    imagenUrl = imagenUrl,
    destacado = destacado,
    opciones = opciones.map { it.aModelo() }
)

@Suppress("unused")
fun Producto.aEntity() = ProductoEntity(
    id = id,
    categoriaId = categoriaId,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    imagenUrl = imagenUrl,
    destacado = destacado
)
