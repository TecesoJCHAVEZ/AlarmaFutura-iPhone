# Instrucciones para Claude — Alarma Futura

## Objetivo único
Mantener una app mínima de iPhone que permita:
1. escribir el nombre de una alarma;
2. seleccionar una fecha y hora futuras;
3. programar una alarma real del sistema con AlarmKit;
4. listar y cancelar alarmas programadas.

## Regla de no regresión
No añadas cuentas, anuncios, compras, servidor, analítica, calendario, repetición, cuenta regresiva, Live Activity ni funciones ajenas al objetivo. No sustituyas AlarmKit por notificaciones locales.

## Compatibilidad
- SwiftUI.
- AlarmKit.
- iOS 26 como destino mínimo.
- Xcode 26 en GitHub Actions mediante `macos-26`.
- El proyecto debe compilar para `iphonesimulator` con firma desactivada.

## Validación obligatoria
Antes de proponer cambios:
1. revisa el registro completo de GitHub Actions;
2. identifica el error exacto, archivo y línea;
3. confirma la firma vigente de la API en la documentación oficial de Apple;
4. realiza el cambio mínimo necesario;
5. conserva `NSAlarmKitUsageDescription`;
6. ejecuta o deja que GitHub Actions ejecute `./scripts/build_ci.sh`;
7. no declares éxito hasta que el check **Compilar app iPhone** termine en verde.

## Archivos principales
- `AlarmaFutura/AlarmaFutura/AlarmStore.swift`: autorización, programación y cancelación.
- `AlarmaFutura/AlarmaFutura/ContentView.swift`: interfaz mínima.
- `AlarmaFutura/AlarmaFutura/AlarmModel.swift`: modelo persistente.
- `.github/workflows/build-ios.yml`: compilación macOS/Xcode.
- `scripts/build_ci.sh`: comando reproducible de compilación.

## Seguridad
Nunca agregues claves, contraseñas, certificados ni perfiles de aprovisionamiento al repositorio. La primera fase solo compila para simulador sin firma.
