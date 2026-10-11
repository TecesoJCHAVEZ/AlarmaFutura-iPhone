# Alarma Futura — Android

Versión Android de Alarma Futura.

## Funciones v1.1

- Identidad visual Alarma Futura + “by: OMNAREL Software”.
- Varias alarmas visibles y almacenadas simultáneamente.
- Alarmas para el mismo día siempre que la hora/minutos sean futuros.
- AlarmManager con alarma exacta.
- Notificación de categoría ALARM y pantalla completa.
- Sonido de alarma y vibración.
- Cancelación individual.
- Reprogramación después de reiniciar el dispositivo.
- Sin servidor, cuenta, anuncios ni conexión a internet.

## Instalación de prueba

El workflow `.github/workflows/build-android.yml` genera un APK Debug instalable:
`AlarmaFutura-Android-debug.apk`.

En Android 12 o posterior, la primera ejecución puede solicitar acceso a
“Alarmas y recordatorios”. En Android 13 o posterior también se solicita
permiso para notificaciones.

## Seguridad

El proyecto no contiene claves, tokens, contraseñas ni secretos. Las imágenes
de marca son recursos locales y la app no requiere credenciales.
