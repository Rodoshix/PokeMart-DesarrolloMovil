package com.pokermart.ecommerce.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.repository.RepositorioAutenticacion
import com.pokermart.ecommerce.data.repository.ResultadoAutenticacion
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val gestorSesion: SessionManager
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado = _estado.asStateFlow()

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

    fun iniciarSesion() {
        val correo = _estado.value.correo
        val contrasena = _estado.value.contrasena
        // En login no obligamos a validar la seguridad de la contrasena para permitir usuarios precargados.
        val correoError = Validadores.validarCorreo(correo)
        if (correoError != null) {
            _estado.value = _estado.value.copy(
                correoError = correoError,
                contrasenaError = null,
                mensajeErrorGeneral = null
            )
            return
        }

        _estado.value = _estado.value.copy(cargando = true, mensajeErrorGeneral = null)

        viewModelScope.launch {
            when (val resultado = repositorioAutenticacion.iniciarSesion(correo, contrasena)) {
                is ResultadoAutenticacion.CredencialesInvalidas -> {
                    _estado.value = _estado.value.copy(
                        mensajeErrorGeneral = "Correo o contrasena incorrectos.",
                        cargando = false
                    )
                }

                is ResultadoAutenticacion.Exito -> {
                    gestorSesion.guardarSesion(resultado.usuario)
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        usuarioAutenticado = resultado.usuario
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
}
