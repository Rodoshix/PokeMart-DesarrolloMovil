package com.pokermart.ecommerce.ui.profile

import android.net.Uri

sealed class ProfileEvent {
    data class SolicitarCamara(val uri: Uri) : ProfileEvent()
    object SolicitarGaleria : ProfileEvent()
    object SesionCerrada : ProfileEvent()
    data class MostrarError(val mensaje: String) : ProfileEvent()
}
