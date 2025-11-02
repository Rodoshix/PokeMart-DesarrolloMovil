package com.pokermart.ecommerce.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pokermart.ecommerce.data.model.Direccion

@Entity(
    tableName = "direccion",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuario_id")]
)
data class DireccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "usuario_id") val usuarioId: Long,
    val etiqueta: String?,
    @ColumnInfo(name = "address_line") val addressLine: String,
    val referencia: String?,
    val latitud: Double?,
    val longitud: Double?,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

fun DireccionEntity.aModelo(): Direccion = Direccion(
    id = id,
    usuarioId = usuarioId,
    etiqueta = etiqueta,
    direccion = addressLine,
    referencia = referencia,
    latitud = latitud,
    longitud = longitud,
    esPredeterminada = isDefault,
    creadoEl = createdAt
)

fun Direccion.aEntidad(): DireccionEntity = DireccionEntity(
    id = id,
    usuarioId = usuarioId,
    etiqueta = etiqueta,
    addressLine = direccion,
    referencia = referencia,
    latitud = latitud,
    longitud = longitud,
    isDefault = esPredeterminada,
    createdAt = creadoEl
)
