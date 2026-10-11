# Alarma Futura — Android

Port Android independiente de la aplicación Alarma Futura.

## Funciones

- Nombre de alarma.
- Cualquier fecha y hora futuras.
- AlarmManager con alarma exacta.
- Notificación de categoría ALARM y pantalla completa.
- Sonido de alarma y vibración.
- Lista local de alarmas.
- Cancelación.
- Reprogramación después de reiniciar el dispositivo.
- Sin servidor, cuenta, anuncios ni conexión a internet.

## Instalación de prueba

El workflow `.github/workflows/build-android.yml` genera un APK Debug instalable:
`AlarmaFutura-Android-debug.apk`.

En Android 12 o posterior, la primera ejecución puede solicitar acceso a
"Alarmas y recordatorios". En Android 13 o posterior también se solicita
permiso para notificaciones.

## Seguridad

El proyecto no contiene claves, tokens, contraseñas ni secretos.
