package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.UsuarioDao
import com.pokermart.ecommerce.data.database.entities.UsuarioEntity
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ResultadoAutenticacion {
    data class Exito(val usuario: Usuario) : ResultadoAutenticacion()
    object CredencialesInvalidas : ResultadoAutenticacion()
}

class RepositorioAutenticacion(
    private val usuarioDao: UsuarioDao
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAutenticacion =
        withContext(Dispatchers.IO) {
            val usuario = usuarioDao.buscarPorCorreo(correo)
            if (usuario == null || usuario.contrasena != contrasena) {
                ResultadoAutenticacion.CredencialesInvalidas
            } else {
                ResultadoAutenticacion.Exito(usuario.aModelo())
            }
        }

    suspend fun registrar(nombre: String, correo: String, contrasena: String): Usuario =
        withContext(Dispatchers.IO) {
            val nuevoUsuario = UsuarioEntity(
                id = System.currentTimeMillis(),
                nombre = nombre,
                correo = correo.lowercase(),
                contrasena = contrasena
            )
            usuarioDao.insertar(nuevoUsuario)
            nuevoUsuario.aModelo()
        }
}
