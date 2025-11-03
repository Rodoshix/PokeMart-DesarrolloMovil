package com.pokermart.ecommerce.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Usuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val apellido: String? = null,
    val region: String? = null,
    val comuna: String? = null,
    val direccion: String? = null,
    val run: String? = null,
    val fechaNacimiento: String? = null,
    val fotoLocal: String? = null
)

fun UsuarioEntity.aModelo() = Usuario(
    id = id,
    nombre = nombre,
    correo = correo,
    apellido = apellido,
    region = region,
    comuna = comuna,
    direccion = direccion,
    run = run,
    fechaNacimiento = fechaNacimiento,
    fotoLocal = fotoLocal
)

fun Usuario.aEntity(contrasena: String) = UsuarioEntity(
    id = id,
    nombre = nombre,
    correo = correo,
    contrasena = contrasena,
    apellido = apellido,
    region = region,
    comuna = comuna,
    direccion = direccion,
    run = run,
    fechaNacimiento = fechaNacimiento,
    fotoLocal = fotoLocal
)

fun UsuarioEntity.conDatosActualizados(desde: Usuario) = copy(
    nombre = desde.nombre,
    correo = desde.correo,
    apellido = desde.apellido,
    region = desde.region,
    comuna = desde.comuna,
    direccion = desde.direccion,
    run = desde.run,
    fechaNacimiento = desde.fechaNacimiento,
    fotoLocal = desde.fotoLocal
)
