package com.pokermart.ecommerce.pref

import android.content.Context
import androidx.core.content.edit
import com.pokermart.ecommerce.data.model.Usuario

class SessionManager(contexto: Context) {

    private val preferencias = contexto.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    fun guardarSesion(usuario: Usuario) {
        preferencias.edit {
            putLong(LLAVE_ID, usuario.id)
            putString(LLAVE_NOMBRE, usuario.nombre)
            putString(LLAVE_CORREO, usuario.correo)
            putString(LLAVE_APELLIDO, usuario.apellido)
            putString(LLAVE_REGION, usuario.region)
            putString(LLAVE_COMUNA, usuario.comuna)
            putString(LLAVE_DIRECCION, usuario.direccion)
            putString(LLAVE_RUN, usuario.run)
            putString(LLAVE_FECHA_NACIMIENTO, usuario.fechaNacimiento)
            putString(LLAVE_FOTO_LOCAL, usuario.fotoLocal)
        }
    }

    fun obtenerSesion(): Usuario? {
        val id = preferencias.getLong(LLAVE_ID, VALOR_INVALIDO)
        val nombre = preferencias.getString(LLAVE_NOMBRE, null)
        val correo = preferencias.getString(LLAVE_CORREO, null)
        if (id == VALOR_INVALIDO || nombre.isNullOrBlank() || correo.isNullOrBlank()) {
            return null
        }
        return Usuario(
            id = id,
            nombre = nombre,
            correo = correo,
            apellido = preferencias.getString(LLAVE_APELLIDO, null),
            region = preferencias.getString(LLAVE_REGION, null),
            comuna = preferencias.getString(LLAVE_COMUNA, null),
            direccion = preferencias.getString(LLAVE_DIRECCION, null),
            run = preferencias.getString(LLAVE_RUN, null),
            fechaNacimiento = preferencias.getString(LLAVE_FECHA_NACIMIENTO, null),
            fotoLocal = preferencias.getString(LLAVE_FOTO_LOCAL, null)
        )
    }

    fun cerrarSesion() {
        preferencias.edit {
            clear()
        }
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "sesion_pokemart"
        const val LLAVE_ID = "usuario_id"
        const val LLAVE_NOMBRE = "usuario_nombre"
        const val LLAVE_CORREO = "usuario_correo"
        const val LLAVE_APELLIDO = "usuario_apellido"
        const val LLAVE_REGION = "usuario_region"
        const val LLAVE_COMUNA = "usuario_comuna"
        const val LLAVE_DIRECCION = "usuario_direccion"
        const val LLAVE_RUN = "usuario_run"
        const val LLAVE_FECHA_NACIMIENTO = "usuario_fecha_nacimiento"
        const val LLAVE_FOTO_LOCAL = "usuario_foto_local"
        const val VALOR_INVALIDO = -1L
    }
}
