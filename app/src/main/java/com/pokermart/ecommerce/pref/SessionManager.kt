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
            correo = correo
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
        const val VALOR_INVALIDO = -1L
    }
}
