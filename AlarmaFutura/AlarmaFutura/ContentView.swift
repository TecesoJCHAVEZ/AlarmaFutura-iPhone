import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var alarmStore: AlarmStore

    @State private var alarmName = ""
    @State private var selectedDate = Calendar.current.date(
        byAdding: .month,
        value: 1,
        to: Date()
    ) ?? Date().addingTimeInterval(2_592_000)

    @State private var isScheduling = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    @State private var isShowingAlert = false

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Ejemplo: Vuelo a Lituania", text: $alarmName)
                        .textInputAutocapitalization(.sentences)

                    DatePicker(
                        "Fecha y hora",
                        selection: $selectedDate,
                        in: Date().addingTimeInterval(60)...,
                        displayedComponents: [.date, .hourAndMinute]
                    )
                    .datePickerStyle(.graphical)
                } header: {
                    Text("Nueva alarma")
                } footer: {
                    Text("La alarma sonará como un despertador del sistema, incluso si el iPhone está en silencio o con un modo de Concentración activo.")
                }

                Section {
                    Button {
                        programAlarm()
                    } label: {
                        HStack {
                            Spacer()
                            if isScheduling {
                                ProgressView()
                            } else {
                                Label("Programar alarma", systemImage: "alarm.fill")
                                    .fontWeight(.semibold)
                            }
                            Spacer()
                        }
                    }
                    .disabled(
                        isScheduling ||
                        alarmName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    )
                }

                if !alarmStore.alarms.isEmpty {
                    Section("Alarmas programadas") {
                        ForEach(alarmStore.alarms) { alarm in
                            AlarmRow(alarm: alarm) {
                                cancel(alarm)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Alarma Futura")
            .alert(alertTitle, isPresented: $isShowingAlert) {
                Button("Aceptar", role: .cancel) { }
            } message: {
                Text(alertMessage)
            }
        }
    }

    private func programAlarm() {
        isScheduling = true

        Task {
            defer { isScheduling = false }

            do {
                try await alarmStore.scheduleAlarm(
                    name: alarmName,
                    fireDate: selectedDate
                )

                alertTitle = "Alarma programada"
                alertMessage = "\(alarmName) sonará el \(selectedDate.formatted(date: .long, time: .shortened))."
                alarmName = ""
                isShowingAlert = true
            } catch {
                alertTitle = "No se pudo programar"
                alertMessage = error.localizedDescription
                isShowingAlert = true
            }
        }
    }

    private func cancel(_ alarm: SavedAlarm) {
        do {
            try alarmStore.cancelAlarm(alarm)
        } catch {
            alertTitle = "No se pudo cancelar"
            alertMessage = error.localizedDescription
            isShowingAlert = true
        }
    }
}

private struct AlarmRow: View {
    let alarm: SavedAlarm
    let onCancel: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "alarm.fill")
                .font(.title2)
                .foregroundStyle(.orange)

            VStack(alignment: .leading, spacing: 3) {
                Text(alarm.name)
                    .font(.headline)

                Text(alarm.fireDate.formatted(date: .abbreviated, time: .shortened))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button(role: .destructive, action: onCancel) {
                Image(systemName: "trash")
            }
            .buttonStyle(.borderless)
            .accessibilityLabel("Cancelar alarma")
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    ContentView()
        .environmentObject(AlarmStore())
}
