package com.pokermart.ecommerce.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.ui.common.BotonPrincipalPokeMart
import com.pokermart.ecommerce.ui.common.CampoTextoPokeMart

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: (Usuario) -> Unit,
    onVolverLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.usuarioRegistrado) {
        val usuario = estado.usuarioRegistrado ?: return@LaunchedEffect
        onRegistroExitoso(usuario)
        viewModel.consumirUsuarioRegistrado()
    }

    LaunchedEffect(estado.mensajeErrorGeneral) {
        val mensaje = estado.mensajeErrorGeneral ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajeGeneral()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Completa tus datos para unirte a PokeMart.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CampoTextoPokeMart(
                valor = estado.nombre,
                enCambio = viewModel::actualizarNombre,
                etiqueta = "Nombre",
                error = estado.nombreError
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampoTextoPokeMart(
                valor = estado.apellido,
                enCambio = viewModel::actualizarApellido,
                etiqueta = "Apellido",
                error = estado.apellidoError
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampoTextoPokeMart(
                valor = estado.correo,
                enCambio = viewModel::actualizarCorreo,
                etiqueta = "Correo electronico",
                error = estado.correoError
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampoTextoPokeMart(
                valor = estado.contrasena,
                enCambio = viewModel::actualizarContrasena,
                etiqueta = "Contrasena",
                error = estado.contrasenaError,
                esContrasena = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampoTextoPokeMart(
                valor = estado.confirmacion,
                enCambio = viewModel::actualizarConfirmacion,
                etiqueta = "Confirmar contrasena",
                error = estado.confirmacionError,
                esContrasena = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            BotonPrincipalPokeMart(
                texto = if (estado.cargando) "Creando..." else "Registrar",
                habilitado = !estado.cargando,
                enClick = viewModel::registrar,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.cargando) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onVolverLogin, enabled = !estado.cargando) {
                Text(text = "Ya tienes cuenta? Inicia sesion")
            }
        }
    }
}
