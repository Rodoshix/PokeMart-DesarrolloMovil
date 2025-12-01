package com.pokermart.ecommerce.ui.address

import com.pokermart.ecommerce.MainDispatcherRule
import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import com.pokermart.ecommerce.data.database.entities.aEntidad
import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class EnviarAViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun vm(direcciones: List<Direccion> = emptyList()): EnviarAViewModel {
        val repo = fakeDireccionesRepo(direcciones)
        val session = Mockito.mock(SessionManager::class.java).apply {
            Mockito.`when`(obtenerSesion()).thenReturn(Usuario(id = 1, nombre = "Test", correo = "a@b.com"))
        }
        return EnviarAViewModel(repo, session)
    }

    @Test
    fun `guardar direccion sin region da error`() = runTest {
        val vm = vm()
        vm.actualizarDireccion("Calle 1")
        vm.guardarDireccion()
        assertTrue(vm.uiState.value.formulario.errorRegion != null)
    }
}

private fun fakeDireccionesRepo(
    direcciones: List<Direccion>
) = RepositorioDirecciones(
    direccionDao = object : DireccionDao {
        private val flow = MutableStateFlow(direcciones.map { it.aEntidad() })
        override fun observarPredeterminada(usuarioId: Long): Flow<DireccionEntity?> = flow.map { it.firstOrNull { dir -> dir.isDefault } }
        override fun observarTodas(usuarioId: Long): Flow<List<DireccionEntity>> = flow
        override suspend fun guardar(direccion: DireccionEntity): Long {
            val lista = flow.value.toMutableList()
            val nuevoId = (lista.maxOfOrNull { it.id } ?: 0L) + 1
            val base = if (direccion.id == 0L) direccion.copy(id = nuevoId) else direccion
            val debeSerDefault = base.isDefault || lista.isEmpty()
            val nueva = base.copy(isDefault = debeSerDefault)
            if (direccion.isDefault) {
                lista.replaceAll { it.copy(isDefault = false) }
            }
            lista.add(nueva)
            flow.value = lista
            return nueva.id
        }

        override suspend fun establecerPredeterminada(id: Long) {
            flow.value = flow.value.map { it.copy(isDefault = it.id == id) }
        }

        override suspend fun limpiarPredeterminadas(usuarioId: Long) {
            flow.value = flow.value.map { it.copy(isDefault = false) }
        }

        override suspend fun contarPorUsuario(usuarioId: Long): Int = flow.value.size
        override suspend fun contarTotal(): Int = flow.value.size
        override suspend fun contarPredeterminadas(usuarioId: Long): Int = flow.value.count { it.isDefault }
        override suspend fun obtenerPorId(id: Long): DireccionEntity? = flow.value.firstOrNull { it.id == id }
        override suspend fun obtenerMasReciente(usuarioId: Long): DireccionEntity? = flow.value.lastOrNull()
        override suspend fun eliminar(id: Long) {
            flow.value = flow.value.filterNot { it.id == id }
        }
    }
)
