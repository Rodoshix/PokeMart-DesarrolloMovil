package com.pokermart.ecommerce.ui.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnviarAViewModel(
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnviarAUiState())
    val uiState = _uiState.asStateFlow()

    private var usuarioId: Long? = null
    private var direccionesActuales: List<Direccion> = emptyList()
    private var observando = false

    init {
        inicializar()
    }

    private fun inicializar() {
        val idSesion = sessionManager.obtenerSesion()?.id
        if (idSesion == null) {
            _uiState.update {
                it.copy(cargando = false, mensaje = "Debe iniciar sesion para gestionar direcciones.")
            }
            return
        }
        if (usuarioId == idSesion && observando) return
        usuarioId = idSesion
        observarDirecciones(idSesion)
    }

    private fun observarDirecciones(userId: Long) {
        observando = true
        viewModelScope.launch {
            repositorioDirecciones.observarTodas(userId).collectLatest { direcciones ->
                direccionesActuales = direcciones
                _uiState.update { estadoActual ->
                    val formulario = estadoActual.formulario
                    estadoActual.copy(
                        cargando = false,
                        direcciones = direcciones.map { it.aUi() },
                        formulario = if (!estadoActual.mostrarDialogo) formulario else formulario.copy(
                            marcarComoPredeterminada = formulario.marcarComoPredeterminada || direcciones.none { dir -> dir.esPredeterminada }
                        )
                    )
                }
            }
        }
    }

    fun abrirDialogoNuevaDireccion() {
        val debeSerDefault = direccionesActuales.none { it.esPredeterminada }
        _uiState.update {
            it.copy(
                mostrarDialogo = true,
                formulario = DireccionFormState(
                    marcarComoPredeterminada = debeSerDefault
                )
            )
        }
    }

    fun editarDireccion(id: Long) {
        val direccion = direccionesActuales.firstOrNull { it.id == id } ?: return
        _uiState.update {
            it.copy(
                mostrarDialogo = true,
                formulario = DireccionFormState(
                    id = direccion.id,
                    etiqueta = direccion.etiqueta.orEmpty(),
                    direccion = direccion.direccion,
                    referencia = direccion.referencia.orEmpty(),
                    latitud = direccion.latitud,
                    longitud = direccion.longitud,
                    marcarComoPredeterminada = direccion.esPredeterminada
                )
            )
        }
    }

    fun solicitarEliminarDireccion(id: Long) {
        _uiState.update {
            it.copy(confirmacionEliminarId = id, eliminando = false)
        }
    }

    fun cancelarEliminacion() {
        _uiState.update {
            it.copy(confirmacionEliminarId = null, eliminando = false)
        }
    }

    fun prepararFormularioConUbicacion(
        direccion: String,
        latitud: Double?,
        longitud: Double?
    ) {
        val debeSerDefault = direccionesActuales.none { it.esPredeterminada }
        _uiState.update {
            it.copy(
                mostrarDialogo = true,
                formulario = DireccionFormState(
                    direccion = direccion,
                    latitud = latitud,
                    longitud = longitud,
                    marcarComoPredeterminada = debeSerDefault
                )
            )
        }
    }

    fun actualizarEtiqueta(valor: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(etiqueta = valor))
        }
    }

    fun actualizarRegion(valor: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    region = valor,
                    errorRegion = null
                )
            )
        }
    }

    fun actualizarCiudad(valor: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    ciudad = valor,
                    errorCiudad = null
                )
            )
        }
    }

    fun actualizarDireccion(valor: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(direccion = valor, errorDireccion = null))
        }
    }

    fun actualizarReferencia(valor: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(referencia = valor))
        }
    }

    fun actualizarPredeterminada(valor: Boolean) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(marcarComoPredeterminada = valor))
        }
    }

    fun cerrarDialogo() {
        _uiState.update {
            it.copy(mostrarDialogo = false, formulario = DireccionFormState(), guardando = false)
        }
    }

    fun guardarDireccion() {
        val userId = usuarioId ?: run {
            _uiState.update {
                it.copy(mensaje = "No se encontro al usuario.")
            }
            return
        }
        val formulario = _uiState.value.formulario
        val regionTexto = formulario.region.trim()
        val ciudadTexto = formulario.ciudad.trim()
        val direccionTexto = formulario.direccion.trim()
        if (regionTexto.isEmpty()) {
            _uiState.update {
                it.copy(formulario = formulario.copy(errorRegion = "La region es obligatoria."))
            }
            return
        }
        if (ciudadTexto.isEmpty()) {
            _uiState.update {
                it.copy(formulario = formulario.copy(errorCiudad = "La ciudad es obligatoria."))
            }
            return
        }
        if (direccionTexto.isEmpty()) {
            _uiState.update {
                it.copy(formulario = formulario.copy(errorDireccion = "La direccion es obligatoria."))
            }
            return
        }
        val etiqueta = formulario.etiqueta.trim().ifEmpty { null }
        val referencia = formulario.referencia.trim().ifEmpty { null }
        val direccionExistente = formulario.id?.let { id ->
            direccionesActuales.firstOrNull { it.id == id }
        }
        if (!formulario.marcarComoPredeterminada) {
            val existeOtraPredeterminada = direccionesActuales.any { direccion ->
                direccion.esPredeterminada && (formulario.id == null || direccion.id != formulario.id)
            }
            if (!existeOtraPredeterminada) {
                _uiState.update {
                    it.copy(mensaje = "Necesitas dejar al menos una direccion predeterminada.")
                }
                return
            }
        }
        val direccionCompuesta = "$regionTexto, $ciudadTexto, $direccionTexto"
        val direccion = Direccion(
            id = formulario.id ?: 0L,
            usuarioId = userId,
            etiqueta = etiqueta,
            direccion = direccionCompuesta,
            referencia = referencia,
            latitud = formulario.latitud ?: direccionExistente?.latitud,
            longitud = formulario.longitud ?: direccionExistente?.longitud,
            esPredeterminada = formulario.marcarComoPredeterminada,
            creadoEl = direccionExistente?.creadoEl ?: System.currentTimeMillis()
        )

        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            try {
                repositorioDirecciones.guardar(
                    direccion = direccion,
                    marcarComoPredeterminada = formulario.marcarComoPredeterminada
                )
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mostrarDialogo = false,
                        formulario = DireccionFormState(),
                        mensaje = "Direccion guardada correctamente."
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = ex.message ?: "No se pudo guardar la direccion."
                    )
                }
            }
        }
    }

    fun seleccionarPredeterminada(id: Long) {
        val userId = usuarioId ?: return
        viewModelScope.launch {
            try {
                repositorioDirecciones.marcarComoPredeterminada(id, userId)
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(mensaje = ex.message ?: "No se pudo actualizar la direccion predeterminada.")
                }
            }
        }
    }

    fun confirmarEliminacion() {
        val userId = usuarioId ?: run {
            _uiState.update { it.copy(mensaje = "No se encontro al usuario.", confirmacionEliminarId = null) }
            return
        }
        val direccionId = _uiState.value.confirmacionEliminarId ?: return
        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            try {
                repositorioDirecciones.eliminar(direccionId, userId)
                _uiState.update {
                    it.copy(
                        eliminando = false,
                        confirmacionEliminarId = null,
                        mensaje = "Direccion eliminada."
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        eliminando = false,
                        mensaje = ex.message ?: "No se pudo eliminar la direccion."
                    )
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun mostrarMensaje(mensaje: String) {
        _uiState.update { it.copy(mensaje = mensaje) }
    }

    private fun Direccion.aUi() = DireccionItemUi(
        id = id,
        etiqueta = etiqueta,
        direccion = direccion,
        referencia = referencia,
        esPredeterminada = esPredeterminada,
        latitud = latitud,
        longitud = longitud
    )
}
