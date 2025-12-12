# TiendaPokemon
Aplicación móvil (Android, Jetpack Compose) para explorar productos de Pokémon, ver detalles y gestionar un carrito básico.

## Integrantes
- Benjamín Arellano Gallardo.
- Rodrigo Sáez

## Funcionalidades
- Flujo de navegación Compose: login → listado de productos → detalle → carrito (todas las pantallas aún en modo stub).
- Tema Material 3 aplicado a toda la app (`TiendaPokemonTheme`).
- Estructura base para consumo de API con Retrofit/Moshi y manejo de sesiones con DataStore.
- Esqueletos para persistencia local con Room (p. ej., carrito) y modelos de dominio (`Product`, `CartItem`, `User`, `Blog`).

## Instrucciones para ejecutar el proyecto
1. Requisitos: JDK 17, Android SDK con API 36, Android Studio Iguana o superior (o CLI con Gradle Wrapper).
2. Clonar/abrir el proyecto y sincronizar dependencias.
3. Ejecutar en dispositivo/emulador:
   - Desde Android Studio: botón “Run”.
   - Por CLI: `./gradlew assembleDebug` y luego `./gradlew installDebug` con un dispositivo conectado.

## APK firmado y ubicación del archivo .jks
- APK generado en este repo: `app/build/intermediates/apk/debug/app-debug.apk`
- APK release firmado: aún no se genera ni se versiona. Al configurar keystore, se puede compilar con `./gradlew assembleRelease`; quedará en `app/build/outputs/apk/release/`.
- Archivo .jks: no existe en el repositorio. Se sugiere guardarlo fuera de control de versiones (por ejemplo `signing/tiendapokemon.jks`) y definir las credenciales en `~/.gradle/gradle.properties` o variables de entorno.
