package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Categoria

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String
)

fun CategoriaEntity.aModelo() = Categoria(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    imagenUrl = imagenUrl
)

fun Categoria.aEntity() = CategoriaEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    imagenUrl = imagenUrl
)
