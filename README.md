# Alarma Futura para iPhone

Aplicación mínima en SwiftUI que programa una alarma real del sistema para una fecha y hora futuras mediante AlarmKit.

## Funciones

- Nombre de la alarma.
- Fecha y hora futuras.
- Alarma del sistema con sonido predeterminado.
- Lista local de alarmas programadas.
- Cancelación de alarmas.
- Sin cuenta, servidor, anuncios ni internet.

## Estado actual

El proyecto fuente está preparado para validarse automáticamente en GitHub Actions. La compilación inicial determinará si las firmas de AlarmKit empleadas coinciden exactamente con la versión de Xcode 26 disponible en el runner.

## Validación automática en GitHub

El archivo `.github/workflows/build-ios.yml` ejecuta una Mac virtual `macos-26`, compila para iOS Simulator sin firma y conserva:

- el registro completo `xcodebuild.log`;
- la aplicación compilada para simulador, cuando la compilación termina correctamente.

También puede ejecutarse manualmente desde la pestaña **Actions** de GitHub.

## Compilación local

En una Mac con Xcode 26:

```bash
./scripts/build_ci.sh
```

## Instalación posterior en un iPhone

Después de obtener una compilación correcta:

1. Abrir `AlarmaFutura/AlarmaFutura.xcodeproj` en Xcode 26.
2. Seleccionar una cuenta Apple en **Signing & Capabilities**.
3. Conectar un iPhone con iOS 26.
4. Ejecutar la app en el dispositivo.
5. Autorizar AlarmKit.
6. Programar una alarma para dos o tres minutos después y probar con la pantalla bloqueada.

## Requisitos

- Xcode 26 o posterior.
- iOS 26 o posterior.
- Para instalar en un iPhone, Apple ID y firma de desarrollo.

## Nota

El artefacto generado por GitHub es para simulador y no es un `.ipa` instalable en un iPhone. La firma e instalación física constituyen la fase posterior.
