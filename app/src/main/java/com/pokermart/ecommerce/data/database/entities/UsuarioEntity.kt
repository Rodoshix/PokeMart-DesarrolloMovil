package com.pokermart.ecommerce.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Usuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val correo: String,
    val contrasena: String
)

fun UsuarioEntity.aModelo() = Usuario(
    id = id,
    nombre = nombre,
    correo = correo
)
