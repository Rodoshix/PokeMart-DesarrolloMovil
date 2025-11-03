package com.pokermart.ecommerce.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioAutenticacion
import com.pokermart.ecommerce.data.repository.ResultadoRegistro
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _estado = MutableStateFlow(RegisterUiState())
    val estado = _estado.asStateFlow()

    fun actualizarNombre(nuevoNombre: String) {
        _estado.value = _estado.value.copy(
            nombre = nuevoNombre,
            nombreError = null,
            mensajeErrorGeneral = null
        )
    }

    fun actualizarCorreo(nuevoCorreo: String) {
        _estado.value = _estado.value.copy(
            correo = nuevoCorreo,
            correoError = null,
            mensajeErrorGeneral = null
        )
    }

    fun actualizarContrasena(nuevaContrasena: String) {
        _estado.value = _estado.value.copy(
            contrasena = nuevaContrasena,
            contrasenaError = null,
            mensajeErrorGeneral = null
        )
    }

    fun actualizarConfirmacion(nuevaConfirmacion: String) {
        _estado.value = _estado.value.copy(
            confirmacion = nuevaConfirmacion,
            confirmacionError = null,
            mensajeErrorGeneral = null
        )
    }

    fun registrar() {
        val estadoActual = _estado.value
        val nombreError = Validadores.validarCampoObligatorio(estadoActual.nombre, "nombre")
        val correoError = Validadores.validarCorreo(estadoActual.correo)
        val contrasenaError = Validadores.validarContrasena(estadoActual.contrasena)
        val confirmacionError = if (estadoActual.contrasena != estadoActual.confirmacion) {
            "Las contrasenas deben coincidir."
        } else {
            null
        }

        if (nombreError != null || correoError != null || contrasenaError != null || confirmacionError != null) {
            _estado.value = estadoActual.copy(
                nombreError = nombreError,
                correoError = correoError,
                contrasenaError = contrasenaError,
                confirmacionError = confirmacionError,
                mensajeErrorGeneral = null
            )
            return
        }

        _estado.value = estadoActual.copy(cargando = true, mensajeErrorGeneral = null)

        viewModelScope.launch {
            when (val resultado = repositorioAutenticacion.registrar(
                nombre = estadoActual.nombre,
                correo = estadoActual.correo,
                contrasena = estadoActual.contrasena
            )) {
                is ResultadoRegistro.Exito -> {
                    sessionManager.guardarSesion(resultado.usuario)
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        usuarioRegistrado = resultado.usuario
                    )
                }

                ResultadoRegistro.CorreoYaRegistrado -> {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeErrorGeneral = "Este correo ya se encuentra registrado."
                    )
                }

                is ResultadoRegistro.Error -> {
                    val mensaje = resultado.mensaje ?: "Ocurrio un error al registrar. Intenta mas tarde."
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensajeErrorGeneral = mensaje
                    )
                }
            }
        }
    }

    fun limpiarMensajeGeneral() {
        if (_estado.value.mensajeErrorGeneral != null) {
            _estado.value = _estado.value.copy(mensajeErrorGeneral = null)
        }
    }

    fun consumirUsuarioRegistrado() {
        if (_estado.value.usuarioRegistrado != null) {
            _estado.value = _estado.value.copy(usuarioRegistrado = null)
        }
    }
}
