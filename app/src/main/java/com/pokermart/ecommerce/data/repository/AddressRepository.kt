package com.pokermart.ecommerce.data.repository

import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.database.entities.aModelo
import com.pokermart.ecommerce.data.model.Direccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RepositorioDirecciones(
    private val direccionDao: DireccionDao
) {

    fun observarPredeterminada(usuarioId: Long): Flow<Direccion?> =
        direccionDao.observarPredeterminada(usuarioId).map { it?.aModelo() }

    fun observarTodas(usuarioId: Long): Flow<List<Direccion>> =
        direccionDao.observarTodas(usuarioId).map { entidades ->
            entidades.map { it.aModelo() }
        }

    suspend fun guardar(direccion: Direccion, marcarComoPredeterminada: Boolean): Direccion =
        withContext(Dispatchers.IO) {
            val entidad = direccion.aEntidad()
            val idGenerado = direccionDao.guardar(entidad)
            val idFinal = if (entidad.id == 0L) idGenerado else entidad.id

            val total = direccionDao.contarPorUsuario(direccion.usuarioId)
            val debeSerDefault = marcarComoPredeterminada || total <= 1 || direccionDao.contarPredeterminadas(direccion.usuarioId) == 0
            if (debeSerDefault) {
                direccionDao.limpiarPredeterminadas(direccion.usuarioId)
                direccionDao.establecerPredeterminada(idFinal)
            }

            direccionDao.obtenerPorId(idFinal)?.aModelo() ?: direccion.copy(
                id = idFinal,
                esPredeterminada = debeSerDefault
            )
        }

    suspend fun marcarComoPredeterminada(direccionId: Long, usuarioId: Long) =
        withContext(Dispatchers.IO) {
            direccionDao.limpiarPredeterminadas(usuarioId)
            direccionDao.establecerPredeterminada(direccionId)
        }

    suspend fun eliminar(direccionId: Long, usuarioId: Long) = withContext(Dispatchers.IO) {
        direccionDao.eliminar(direccionId)
        if (direccionDao.contarPredeterminadas(usuarioId) == 0) {
            val candidata = direccionDao.obtenerMasReciente(usuarioId)
            if (candidata != null) {
                direccionDao.limpiarPredeterminadas(usuarioId)
                direccionDao.establecerPredeterminada(candidata.id)
            }
        }
    }
}
