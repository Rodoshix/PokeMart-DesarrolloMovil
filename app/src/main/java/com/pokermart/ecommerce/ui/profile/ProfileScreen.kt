package com.pokermart.ecommerce.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    usuarioId: Long,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit,
    onGestionarDirecciones: () -> Unit
) {
    val estado = viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val tomarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        viewModel.onFotoCapturada(exito)
        if (!exito) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("No se pudo tomar la foto.")
            }
        }
    }

    val seleccionarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onFotoSeleccionadaDesdeGaleria(uri)
    }

    val solicitarPermisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
        if (concedido) {
            viewModel.tomarFoto()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Necesitamos permiso de camara para usar esta funcion.")
            }
        }
    }

    LaunchedEffect(usuarioId) {
        viewModel.cargar(usuarioId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is ProfileEvent.SolicitarCamara -> {
                    tomarFotoLauncher.launch(evento.uri)
                }

                ProfileEvent.SolicitarGaleria -> {
                    seleccionarFotoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                ProfileEvent.SesionCerrada -> onCerrarSesion()

                is ProfileEvent.MostrarError -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    LaunchedEffect(estado.value.mensajeExito) {
        val mensaje = estado.value.mensajeExito ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajes()
    }

    LaunchedEffect(estado.value.mensajeError) {
        val mensaje = estado.value.mensajeError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajes()
    }

    val fechaIso = estado.value.fechaNacimiento
    val fechaSeleccionadaTexto = remember(fechaIso) {
        fechaIso?.let {
            runCatching {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            }.getOrDefault("Selecciona")
        } ?: "Selecciona"
    }

    val onSeleccionarFecha = remember(context, fechaIso) {
        {
            val calendario = Calendar.getInstance()
            val fechaExistente = fechaIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (fechaExistente != null) {
                calendario.set(fechaExistente.year, fechaExistente.monthValue - 1, fechaExistente.dayOfMonth)
            }
            val minCalendar = Calendar.getInstance().apply {
                set(1900, Calendar.JANUARY, 1)
            }
            val maxCalendar = Calendar.getInstance().apply {
                add(Calendar.YEAR, -14)
            }
            if (calendario.timeInMillis > maxCalendar.timeInMillis) {
                calendario.timeInMillis = maxCalendar.timeInMillis
            }
            if (calendario.timeInMillis < minCalendar.timeInMillis) {
                calendario.timeInMillis = minCalendar.timeInMillis
            }
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val seleccion = LocalDate.of(year, month + 1, dayOfMonth)
                        .format(DateTimeFormatter.ISO_DATE)
                    viewModel.actualizarFechaNacimiento(seleccion)
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            )
            dialog.datePicker.maxDate = maxCalendar.timeInMillis
            dialog.datePicker.minDate = minCalendar.timeInMillis
            dialog.show()
        }
    }
    val fechaFieldInteraction = remember { MutableInteractionSource() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu perfil") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (estado.value.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FotoPerfil(
                    rutaLocal = estado.value.fotoActual,
                    correo = estado.value.correo
                )
                RowAccionesFoto(
                    onTomarFoto = {
                        if (tienePermisoCamara) {
                            viewModel.tomarFoto()
                        } else {
                            solicitarPermisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onElegirGaleria = viewModel::elegirFoto
                )

                OutlinedTextField(
                    value = estado.value.nombre,
                    onValueChange = viewModel::actualizarNombre,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = estado.value.errorNombre != null,
                    supportingText = {
                        estado.value.errorNombre?.let { Text(it, color = Color.Red) }
                    }
                )

                OutlinedTextField(
                    value = estado.value.apellido,
                    onValueChange = viewModel::actualizarApellido,
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.value.region,
                    onValueChange = viewModel::actualizarRegion,
                    label = { Text("Region") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.value.comuna,
                    onValueChange = viewModel::actualizarComuna,
                    label = { Text("Comuna") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.value.direccion,
                    onValueChange = viewModel::actualizarDireccion,
                    label = { Text("Direccion") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = onGestionarDirecciones,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Administrar direcciones guardadas")
                }

                OutlinedTextField(
                    value = estado.value.run,
                    onValueChange = viewModel::actualizarRun,
                    label = { Text("RUN") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    isError = estado.value.errorRun != null,
                    supportingText = {
                        estado.value.errorRun?.let { Text(it, color = Color.Red) }
                    }
                )

                OutlinedTextField(
                    value = fechaSeleccionadaTexto,
                    onValueChange = {},
                    label = { Text("Fecha de nacimiento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = fechaFieldInteraction,
                            indication = null
                        ) { onSeleccionarFecha() },
                    readOnly = true,
                    isError = estado.value.errorFechaNacimiento != null,
                    interactionSource = fechaFieldInteraction,
                    supportingText = {
                        estado.value.errorFechaNacimiento?.let { Text(it, color = Color.Red) }
                    }
                )

                OutlinedTextField(
                    value = estado.value.correo,
                    onValueChange = {},
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = viewModel::guardar,
                    enabled = !estado.value.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.value.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text("Guardar cambios")
                }

                OutlinedButton(
                    onClick = viewModel::cerrarSesion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesion")
                }
            }
        }
    }
}

@Composable
private fun FotoPerfil(
    rutaLocal: String?,
    correo: String
) {
    val imagenModel: Any? = rutaLocal?.let {
        when {
            it.startsWith("content://") || it.startsWith("file://") -> Uri.parse(it)
            else -> File(it)
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (imagenModel != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenModel)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = correo.firstOrNull()?.uppercaseChar()?.toString() ?: "?", color = Color.White)
            }
        }
    }
}

@Composable
private fun RowAccionesFoto(
    onTomarFoto: () -> Unit,
    onElegirGaleria: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onTomarFoto,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tomar foto")
        }
        OutlinedButton(
            onClick = onElegirGaleria,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Elegir de galeria")
        }
    }
}
