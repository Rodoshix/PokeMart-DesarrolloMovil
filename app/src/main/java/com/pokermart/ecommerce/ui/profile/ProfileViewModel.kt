package com.pokermart.ecommerce.ui.profile

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokermart.ecommerce.data.model.Direccion
import com.pokermart.ecommerce.data.model.Usuario
import com.pokermart.ecommerce.data.repository.RepositorioAutenticacion
import com.pokermart.ecommerce.data.repository.RepositorioDirecciones
import com.pokermart.ecommerce.pref.SessionManager
import com.pokermart.ecommerce.utils.Validadores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.math.max

class ProfileViewModel(
    application: Application,
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val repositorioDirecciones: RepositorioDirecciones,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {

    private val _estado = MutableStateFlow(ProfileUiState())
    val estado = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<ProfileEvent>()
    val eventos = _eventos.asSharedFlow()

    private var usuarioActual: Usuario? = null
    private var usuarioId: Long? = null
    private var ultimaFotoArchivo: File? = null

    fun cargar(uid: Long) {
        if (usuarioId == uid && usuarioActual != null && !_estado.value.cargando) return
        usuarioId = uid
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensajeError = null) }
            val usuario = repositorioAutenticacion.obtenerUsuarioPorId(uid)
            if (usuario == null) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "No pudimos cargar tu perfil.",
                        mensajeExito = null
                    )
                }
            } else {
                usuarioActual = usuario
                _estado.update {
                    it.copy(
                        cargando = false,
                        nombre = usuario.nombre,
                        apellido = usuario.apellido.orEmpty(),
                        region = usuario.region.orEmpty(),
                        comuna = usuario.comuna.orEmpty(),
                        direccion = usuario.direccion.orEmpty(),
                        run = usuario.run?.let { runGuardado ->
                            formatearRun(limpiarRun(runGuardado))
                        }.orEmpty(),
                        fechaNacimiento = usuario.fechaNacimiento,
                        correo = usuario.correo,
                        fotoActual = usuario.fotoLocal,
                        mensajeError = null,
                        mensajeExito = null
                    )
                }
            }
        }
    }

    fun actualizarNombre(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun actualizarApellido(valor: String) {
        _estado.update { it.copy(apellido = valor) }
    }

    fun actualizarRegion(valor: String) {
        _estado.update { it.copy(region = valor) }
    }

    fun actualizarComuna(valor: String) {
        _estado.update { it.copy(comuna = valor) }
    }

    fun actualizarDireccion(valor: String) {
        _estado.update { it.copy(direccion = valor) }
    }

    fun actualizarRun(valor: String) {
        val limpio = limpiarRun(valor)
        _estado.update { it.copy(run = formatearRun(limpio), errorRun = null) }
    }

    fun actualizarFechaNacimiento(fechaIso: String?) {
        _estado.update { it.copy(fechaNacimiento = fechaIso, errorFechaNacimiento = null) }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    fun guardar() {
        val usuarioBase = usuarioActual ?: return

        val nombre = _estado.value.nombre.trim()
        val runFormateado = _estado.value.run.trim()
        val runLimpio = limpiarRun(runFormateado)
        val fechaIso = _estado.value.fechaNacimiento

        val errorNombre = Validadores.validarCampoObligatorio(nombre, "nombre")
        val errorRun = if (runLimpio.isNotEmpty()) Validadores.validarRun(runLimpio) else null
        val errorFecha = Validadores.validarFechaNacimientoIso(fechaIso)

        if (errorNombre != null || errorRun != null || errorFecha != null) {
            _estado.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorRun = errorRun,
                    errorFechaNacimiento = errorFecha,
                    mensajeError = null,
                    mensajeExito = null
                )
            }
            return
        }

        _estado.update { it.copy(guardando = true, mensajeError = null, mensajeExito = null) }

        viewModelScope.launch {
            val actualizado = usuarioBase.copy(
                nombre = nombre,
                apellido = _estado.value.apellido.trim().takeIf { it.isNotEmpty() },
                region = _estado.value.region.trim().takeIf { it.isNotEmpty() },
                comuna = _estado.value.comuna.trim().takeIf { it.isNotEmpty() },
                direccion = _estado.value.direccion.trim().takeIf { it.isNotEmpty() },
                run = runLimpio.ifEmpty { null },
                fechaNacimiento = fechaIso,
                fotoLocal = _estado.value.fotoActual
            )

            val resultado = repositorioAutenticacion.actualizarPerfil(actualizado)
            if (resultado == null) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo guardar el perfil, intenta mas tarde."
                    )
                }
            } else {
                usuarioActual = resultado
                sessionManager.guardarSesion(resultado)
                val direccionTexto = resultado.direccion?.trim().orEmpty()
                if (direccionTexto.isNotEmpty()) {
                    sincronizarDireccionPredeterminada(
                        usuarioId = resultado.id,
                        direccion = direccionTexto
                    )
                }
                _estado.update {
                    it.copy(
                        guardando = false,
                        mensajeError = null,
                        mensajeExito = "Perfil actualizado correctamente."
                    )
                }
            }
        }
    }

    fun tomarFoto() {
        viewModelScope.launch {
            val archivo = withContext(Dispatchers.IO) { crearArchivoTemporal() } ?: run {
                _eventos.emit(ProfileEvent.MostrarError("No pudimos preparar la camara."))
                return@launch
            }
            ultimaFotoArchivo = archivo
            val contexto = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
            _eventos.emit(ProfileEvent.SolicitarCamara(uri))
        }
    }

    fun onFotoCapturada(exito: Boolean) {
        val archivo = ultimaFotoArchivo
        ultimaFotoArchivo = null
        if (!exito) {
            archivo?.delete()
            return
        }
        if (archivo != null) {
            _estado.update { it.copy(fotoActual = archivo.absolutePath, mensajeExito = null) }
        }
    }

    fun elegirFoto() {
        viewModelScope.launch {
            _eventos.emit(ProfileEvent.SolicitarGaleria)
        }
    }

    fun onFotoSeleccionadaDesdeGaleria(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val destino = copiarUriALocal(uri)
            if (destino == null) {
                _eventos.emit(ProfileEvent.MostrarError("No pudimos copiar la imagen seleccionada."))
            } else {
                _estado.update { it.copy(fotoActual = destino.absolutePath, mensajeExito = null) }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.cerrarSesion()
            _eventos.emit(ProfileEvent.SesionCerrada)
        }
    }

    private fun crearArchivoTemporal(): File? {
        val contexto = getApplication<Application>()
        val directorio = contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: contexto.filesDir
        return try {
            File.createTempFile("perfil_${System.currentTimeMillis()}", ".jpg", directorio)
        } catch (ex: IOException) {
            null
        }
    }

    private suspend fun copiarUriALocal(uri: Uri): File? = withContext(Dispatchers.IO) {
        val contexto = getApplication<Application>()
        val directorio = contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: contexto.filesDir
        return@withContext try {
            val destino = File.createTempFile("galeria_${System.currentTimeMillis()}", ".jpg", directorio)
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            } ?: return@withContext null
            destino
        } catch (ex: IOException) {
            null
        }
    }

    private suspend fun sincronizarDireccionPredeterminada(
        usuarioId: Long,
        direccion: String
    ) {
        try {
            val actual = repositorioDirecciones.observarPredeterminada(usuarioId).firstOrNull()
            val direccionPerfil = Direccion(
                id = actual?.id ?: 0L,
                usuarioId = usuarioId,
                etiqueta = actual?.etiqueta ?: "Perfil",
                direccion = direccion,
                referencia = actual?.referencia,
                latitud = actual?.latitud,
                longitud = actual?.longitud,
                esPredeterminada = true,
                creadoEl = actual?.creadoEl ?: System.currentTimeMillis()
            )
            repositorioDirecciones.guardar(direccionPerfil, marcarComoPredeterminada = true)
        } catch (_: Exception) {
            // Ignoramos errores de sincronizacion para no interrumpir la actualizacion del perfil.
        }
    }

    private fun limpiarRun(valor: String): String {
        val sinPuntuacion = valor.filter { it.isDigit() || it.equals('k', true) }
        return sinPuntuacion.uppercase(Locale.getDefault())
    }

    private fun formatearRun(limpio: String): String {
        if (limpio.isEmpty()) return ""
        if (limpio.length == 1) return limpio
        val cuerpo = limpio.dropLast(1)
        val digito = limpio.last()
        val cuerpoFormateado = construirCuerpoConPuntos(cuerpo)
        return "$cuerpoFormateado-$digito"
    }

    private fun construirCuerpoConPuntos(cuerpo: String): String {
        if (cuerpo.isEmpty()) return ""
        val builder = StringBuilder()
        var indice = cuerpo.length
        while (indice > 0) {
            val inicio = max(0, indice - 3)
            val segmento = cuerpo.substring(inicio, indice)
            if (builder.isNotEmpty()) {
                builder.insert(0, '.')
            }
            builder.insert(0, segmento)
            indice = inicio
        }
        return builder.toString()
    }
}
