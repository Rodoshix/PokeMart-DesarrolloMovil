package com.pokermart.ecommerce.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pokermart.ecommerce.data.model.Categoria
import com.pokermart.ecommerce.data.model.OpcionProducto
import com.pokermart.ecommerce.data.model.Producto
import java.util.Locale

@Composable
fun CampoTextoPokeMart(
    modifier: Modifier = Modifier,
    valor: String,
    enCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    esContrasena: Boolean = false
) {
    val transformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None
    OutlinedTextField(
        value = valor,
        onValueChange = enCambio,
        label = { Text(text = etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        },
        visualTransformation = transformation,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BotonPrincipalPokeMart(
    modifier: Modifier = Modifier,
    texto: String,
    habilitado: Boolean,
    enClick: () -> Unit
) {
    Button(
        onClick = enClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = texto.uppercase())
    }
}

@Composable
fun EstadoVacio(
    modifier: Modifier = Modifier,
    mensaje: String,
    accionTexto: String? = null,
    enAccionClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (accionTexto != null && enAccionClick != null) {
                TextButton(onClick = enAccionClick) {
                    Text(text = accionTexto)
                }
            }
        }
    }
}

@Composable
@Suppress("unused")
fun TarjetaCategoria(
    modifier: Modifier = Modifier,
    categoria: Categoria,
    enClick: (Categoria) -> Unit
) {
    Card(
        onClick = { enClick(categoria) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = categoria.imagenUrl,
                contentDescription = categoria.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = categoria.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TarjetaProducto(
    modifier: Modifier = Modifier,
    producto: Producto,
    enClick: (Producto) -> Unit
) {
    Card(
        onClick = { enClick(producto) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$" + String.format(Locale("es", "CL"), "%.2f", producto.precio),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodySmall
            )
            if (producto.destacado) {
                DestacadoChip(texto = "Destacado")
            }
        }
    }
}

@Composable
fun TarjetaOpcionProducto(
    modifier: Modifier = Modifier,
    opcion: OpcionProducto,
    precioBase: Double,
    seleccionado: Boolean,
    onSeleccionar: () -> Unit
) {
    Card(
        onClick = onSeleccionar,
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (seleccionado) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = opcion.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = opcion.descripcion, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = seleccionado,
                    onClick = onSeleccionar
                )
                val cantidad = opcion.extraerCantidad()
                val precioFinal = if (cantidad != null && cantidad > 0) {
                    (precioBase * cantidad) + opcion.precioExtra
                } else {
                    precioBase + opcion.precioExtra
                }
                val totalTexto = "$" + String.format(Locale("es", "CL"), "%.2f", precioFinal)
                val precioPorUnidadTexto = cantidad
                    ?.takeIf { it > 0 }
                    ?.let { count -> "$" + String.format(Locale("es", "CL"), "%.2f", precioFinal / count) }
                Text(
                    text = precioPorUnidadTexto?.let { "$totalTexto ($it c/u)" } ?: totalTexto,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Stock: ${opcion.stock}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun OpcionProducto.extraerCantidad(): Int? {
    if (!nombre.contains('x', ignoreCase = true)) return null
    val partes = nombre.lowercase().split('x')
    val numero = partes.lastOrNull()?.filter { it.isDigit() }
    return numero?.toIntOrNull()
}

@Composable
private fun DestacadoChip(
    modifier: Modifier = Modifier,
    texto: String
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
