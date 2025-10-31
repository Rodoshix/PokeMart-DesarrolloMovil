package com.pokermart.ecommerce.utils

import android.util.Patterns

object Validadores {

    fun validarCorreo(correo: String): String? {
        val normalizado = correo.trim()
        if (normalizado.isEmpty()) return "El correo es obligatorio."
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizado).matches()) {
            return "Introduce un correo electronico valido."
        }
        return null
    }

    fun validarContrasena(contrasena: String): String? {
        if (contrasena.length < 6) return "La contrasena debe tener al menos 6 caracteres."
        if (!contrasena.any(Char::isDigit)) return "Incluye al menos un numero en la contrasena."
        return null
    }

    fun validarCampoObligatorio(valor: String, nombreCampo: String): String? {
        if (valor.trim().isEmpty()) return "El campo $nombreCampo es obligatorio."
        return null
    }
}
