package com.pokermart.ecommerce.utils

import android.util.Patterns
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

    fun validarRun(run: String): String? {
        val limpio = run
            .replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()

        if (limpio.length < 2) return "El RUN debe incluir numero y digito verificador."

        val cuerpo = limpio.dropLast(1)
        val digitoVerificadorIngresado = limpio.last()

        if (!cuerpo.all(Char::isDigit)) return "El RUN solo puede contener digitos en el cuerpo."

        val digitoCalculado = calcularDigitoVerificadorRun(cuerpo)
        if (digitoCalculado != digitoVerificadorIngresado) {
            return "El RUN ingresado no es valido."
        }
        return null
    }

    fun validarFechaNacimientoIso(fechaIso: String?): String? {
        if (fechaIso.isNullOrBlank()) return "Selecciona tu fecha de nacimiento."
        return try {
            val fecha = LocalDate.parse(fechaIso, DateTimeFormatter.ISO_DATE)
            if (fecha.year < 1900) return "La fecha de nacimiento no puede ser anterior a 1900."
            val edad = Period.between(fecha, LocalDate.now()).years
            if (edad < 14) return "Debes tener al menos 14 anos para usar la app."
            null
        } catch (ex: DateTimeParseException) {
            "Formato de fecha invalido."
        }
    }

    private fun calcularDigitoVerificadorRun(cuerpo: String): Char {
        var multiplicador = 2
        var suma = 0
        for (digito in cuerpo.reversed()) {
            suma += (digito.digitToInt()) * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        return when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }
}
