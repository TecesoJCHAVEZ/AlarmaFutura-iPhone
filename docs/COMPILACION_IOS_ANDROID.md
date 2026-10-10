# Construcción virtual de Alarma Futura — iPhone y Android

## Qué crea GitHub Actions

- **iPhone simulador:** `.github/workflows/build-ios.yml` (flujo original).
- **iPhone físico:** `.github/workflows/build-ios-device.yml` genera un archivo Release `.xcarchive` SIN firma. **No es un IPA listo para instalar**.
- **Android:** `.github/workflows/build-android.yml` ejecuta pruebas unitarias, lint, APK debug instalable, APK Release y AAB. APK Release y AAB **no son distribuibles cuando faltan las credenciales de firma**.

Los artefactos de cada ejecución se conservan durante siete días. Descargar desde GitHub → Actions → ejecución → Artifacts.

## Requisitos antes de distribución

### Apple

Cuenta Apple Developer y certificados/perfiles de aprovisionamiento en un entorno seguro. El paso de exportar un `.ipa` firmado no está habilitado; requiere decidir modalidad (desarrollo, Ad Hoc, TestFlight/App Store). El proyecto iOS existente no se altera.

### Google Play / Android

Para firmar el APK Release y AAB en CI, crear los secretos de Actions:
`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
La clave se decodifica de forma efímera en el runner. No guardar claves privadas en Git. Tras firmar, configurar Play Console y pruebas reales.

## Pruebas funcionales imprescindibles

Probar con un iPhone iOS 26 y un Android físico: programar una alarma con el teléfono bloqueado; permisos denegados y concedidos; modos silencio/descanso y restricciones de batería; cancelar; reiniciar dispositivo Android; cambio de zona horaria y de fecha. CI verifica compilación, tests Java y análisis estático, **no garantiza** que una alarma suene en todos los dispositivos.

## Android

Implementación nativa ligera en Java + Android SDK (sin servidor). Precisa permiso de alarma exacta y de notificaciones; la apertura de pantalla completa puede requerir permiso adicional. El servicio de reproducción utiliza una notificación foreground con sonido de alarma. Target Android API 35, min API 26.

## Fuentes

- https://developer.android.com/develop/background-work/services/alarms
- https://developer.android.com/about/versions/14/behavior-changes-14
