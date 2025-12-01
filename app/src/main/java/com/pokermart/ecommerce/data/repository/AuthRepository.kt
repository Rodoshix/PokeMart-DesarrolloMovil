package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.UsuarioDao
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.database.entities.conDatosActualizados
import com.pokermart.ecommerce.data.database.entities.UsuarioEntity
import com.pokermart.ecommerce.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ResultadoAutenticacion {
    data class Exito(val usuario: Usuario) : ResultadoAutenticacion()
    object CredencialesInvalidas : ResultadoAutenticacion()
}

sealed class ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro()
    object CorreoYaRegistrado : ResultadoRegistro()
    data class Error(val mensaje: String? = null) : ResultadoRegistro()
}

class RepositorioAutenticacion(
    private val usuarioDao: UsuarioDao
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAutenticacion =
        withContext(Dispatchers.IO) {
            val correoNormalizado = correo.trim().lowercase()
            val usuario = usuarioDao.buscarPorCorreo(correoNormalizado)
            if (usuario == null || usuario.contrasena != contrasena) {
                ResultadoAutenticacion.CredencialesInvalidas
            } else {
                ResultadoAutenticacion.Exito(usuario.aModelo())
            }
        }

    suspend fun registrar(nombre: String, apellido: String, correo: String, contrasena: String): ResultadoRegistro =
        withContext(Dispatchers.IO) {
            val correoNormalizado = correo.trim().lowercase()
            val existente = usuarioDao.buscarPorCorreo(correoNormalizado)
            if (existente != null) {
                return@withContext ResultadoRegistro.CorreoYaRegistrado
            }
            val nuevoUsuario = UsuarioEntity(
                id = System.currentTimeMillis(),
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                correo = correoNormalizado,
                contrasena = contrasena
            )
            return@withContext try {
                usuarioDao.insertar(nuevoUsuario)
                ResultadoRegistro.Exito(nuevoUsuario.aModelo())
            } catch (ex: Exception) {
                ResultadoRegistro.Error(ex.message)
            }
        }

    suspend fun obtenerUsuarioPorId(id: Long): Usuario? = withContext(Dispatchers.IO) {
        usuarioDao.obtenerPorId(id)?.aModelo()
    }

    suspend fun actualizarPerfil(usuario: Usuario): Usuario? = withContext(Dispatchers.IO) {
        val actual = usuarioDao.obtenerPorId(usuario.id) ?: return@withContext null
        val actualizado = actual.conDatosActualizados(usuario)
        usuarioDao.actualizar(actualizado)
        actualizado.aModelo()
    }
}
