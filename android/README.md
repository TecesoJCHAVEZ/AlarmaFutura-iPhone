# Alarma Futura — Android

Versión Android de Alarma Futura.

## Funciones v1.1.1

- Identidad visual Alarma Futura + “by: OMNAREL Software”.
- Varias alarmas visibles y almacenadas simultáneamente.
- Alarmas para el mismo día siempre que la hora/minutos sean futuros.
- AlarmManager con alarma exacta.
- Servicio en primer plano dedicado a la alarma activa.
- Sonido de alarma en bucle hasta que el usuario pulse “Detener”.
- Vibración repetitiva mientras la alarma está activa.
- Wake lock parcial durante la alarma para mantener audio/vibración con pantalla apagada.
- Notificación persistente de categoría ALARM y pantalla completa cuando Android lo permite.
- Botón para habilitar acceso de pantalla completa en Android 14 o posterior.
- Cancelación individual de alarmas futuras.
- Reprogramación después de reiniciar el dispositivo.
- Sin servidor, cuenta, anuncios ni conexión a internet.

## Instalación de prueba

El workflow `.github/workflows/build-android.yml` genera un APK Debug instalable:
`AlarmaFutura-Android-debug.apk`.

En Android 12 o posterior, la primera ejecución puede solicitar acceso a
“Alarmas y recordatorios”. En Android 13 o posterior también se solicita
permiso para notificaciones. En Android 14 o posterior puede aparecer el botón
“Permitir alarma en pantalla completa”; conviene habilitarlo para que la pantalla
de alarma pueda mostrarse sobre la pantalla bloqueada.

## Prueba recomendada

1. Confirmar que “Alarmas y recordatorios” está permitido.
2. En Android 14+, habilitar “pantalla completa” desde la app.
3. Verificar que el volumen de alarma del teléfono no esté en cero.
4. Programar una alarma a 2 o 3 minutos.
5. Bloquear la pantalla y dejar la app en segundo plano.
6. La alarma debe reproducir sonido y vibración de forma continua hasta pulsar “Detener”.

## Seguridad

El proyecto no contiene claves, tokens, contraseñas ni secretos. Las imágenes
de marca son recursos locales y la app no requiere credenciales.
