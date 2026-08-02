# Primer encargo para Claude Code

Revisa este proyecto iOS de forma conservadora. El objetivo es que el check de GitHub Actions **Compilar app iPhone** termine correctamente en Xcode 26 para iOS Simulator.

Reglas:
- Lee primero `CLAUDE.md`.
- No agregues funciones nuevas ni rediseñes la interfaz.
- No sustituyas AlarmKit por notificaciones locales.
- Revisa especialmente las firmas actuales de `AlarmManager.AlarmConfiguration`, `Alarm.Schedule.fixed`, `AlarmAttributes`, `AlarmPresentation.Alert`, autorización y cancelación en iOS 26.
- Usa solamente documentación oficial de Apple como fuente técnica.
- Corrige únicamente errores comprobados por el registro de compilación.
- Abre una pull request con los cambios y explica cada corrección.
- Considera terminada la tarea únicamente cuando GitHub Actions quede en verde.
