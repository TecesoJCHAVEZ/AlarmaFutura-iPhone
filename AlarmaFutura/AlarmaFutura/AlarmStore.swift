import ActivityKit
import AlarmKit
import Foundation
import SwiftUI

nonisolated struct EmptyAlarmMetadata: AlarmMetadata { }

@MainActor
final class AlarmStore: ObservableObject {
    @Published private(set) var alarms: [SavedAlarm] = []

    private let storageKey = "savedAlarms"

    init() {
        loadSavedAlarms()
    }

    func scheduleAlarm(name: String, fireDate: Date) async throws {
        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanName.isEmpty else {
            throw AlarmAppError.missingName
        }

        guard fireDate > Date().addingTimeInterval(30) else {
            throw AlarmAppError.dateMustBeInFuture
        }

        guard try await requestAuthorizationIfNeeded() else {
            throw AlarmAppError.authorizationDenied
        }

        let id = UUID()

        let stopButton = AlarmButton(
            text: "Detener",
            textColor: .white,
            systemImageName: "stop.circle.fill"
        )

        let alert = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: cleanName),
            stopButton: stopButton
        )

        let attributes = AlarmAttributes<EmptyAlarmMetadata>(
            presentation: AlarmPresentation(alert: alert),
            tintColor: .blue
        )

        let configuration = AlarmManager.AlarmConfiguration<EmptyAlarmMetadata>.alarm(
            schedule: .fixed(fireDate),
            attributes: attributes
        )

        _ = try await AlarmManager.shared.schedule(
            id: id,
            configuration: configuration
        )

        alarms.append(
            SavedAlarm(id: id, name: cleanName, fireDate: fireDate)
        )
        sortAndSave()
    }

    func cancelAlarm(_ alarm: SavedAlarm) throws {
        try AlarmManager.shared.cancel(id: alarm.id)
        alarms.removeAll { $0.id == alarm.id }
        saveAlarms()
    }

    private func requestAuthorizationIfNeeded() async throws -> Bool {
        switch AlarmManager.shared.authorizationState {
        case .authorized:
            return true
        case .notDetermined:
            let newState = try await AlarmManager.shared.requestAuthorization()
            return newState == .authorized
        case .denied:
            return false
        @unknown default:
            return false
        }
    }

    private func sortAndSave() {
        alarms.sort { $0.fireDate < $1.fireDate }
        saveAlarms()
    }

    private func saveAlarms() {
        guard let encoded = try? JSONEncoder().encode(alarms) else { return }
        UserDefaults.standard.set(encoded, forKey: storageKey)
    }

    private func loadSavedAlarms() {
        guard
            let data = UserDefaults.standard.data(forKey: storageKey),
            let decoded = try? JSONDecoder().decode([SavedAlarm].self, from: data)
        else {
            return
        }

        alarms = decoded
            .filter { $0.fireDate > Date() }
            .sorted { $0.fireDate < $1.fireDate }

        saveAlarms()
    }
}

enum AlarmAppError: LocalizedError {
    case missingName
    case dateMustBeInFuture
    case authorizationDenied

    var errorDescription: String? {
        switch self {
        case .missingName:
            return "Escribe un nombre para la alarma."
        case .dateMustBeInFuture:
            return "Para hoy, selecciona una hora y minutos posteriores al momento actual."
        case .authorizationDenied:
            return "El permiso para crear alarmas está desactivado. Actívalo en Ajustes."
        }
    }
}
