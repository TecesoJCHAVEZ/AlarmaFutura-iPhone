import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var alarmStore: AlarmStore

    @State private var alarmName = ""
    @State private var selectedDate = Date().addingTimeInterval(5 * 60)
    @State private var isScheduling = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    @State private var isShowingAlert = false

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    VStack(spacing: 10) {
                        Image("AlarmaFuturaMark")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 92, height: 92)
                            .accessibilityHidden(true)

                        Text("Alarma Futura")
                            .font(.title2)
                            .fontWeight(.bold)

                        Text("Programa varias alarmas para hoy o para cualquier fecha futura.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)

                        HStack(spacing: 7) {
                            Text("by:")
                                .font(.caption)
                                .foregroundStyle(.secondary)

                            Image("OmnarelSoftwareMark")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 28, height: 28)
                                .accessibilityHidden(true)

                            Text("OMNAREL Software")
                                .font(.caption)
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 6)
                }

                Section {
                    TextField("Ejemplo: Llamada importante", text: $alarmName)
                        .textInputAutocapitalization(.sentences)

                    DatePicker(
                        "Fecha",
                        selection: $selectedDate,
                        in: Date().addingTimeInterval(30)...,
                        displayedComponents: [.date]
                    )

                    DatePicker(
                        "Hora",
                        selection: $selectedDate,
                        in: Date().addingTimeInterval(30)...,
                        displayedComponents: [.hourAndMinute]
                    )
                } header: {
                    Text("Nueva alarma")
                } footer: {
                    Text("Puedes crear varias alarmas, incluso para el mismo día. Para hoy, la hora y los minutos deben ser posteriores al momento actual.")
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

                Section {
                    if alarmStore.alarms.isEmpty {
                        HStack(spacing: 10) {
                            Image(systemName: "alarm")
                                .foregroundStyle(.secondary)
                            Text("No hay alarmas programadas.")
                                .foregroundStyle(.secondary)
                        }
                    } else {
                        ForEach(alarmStore.alarms) { alarm in
                            AlarmRow(alarm: alarm) {
                                cancel(alarm)
                            }
                        }
                    }
                } header: {
                    HStack {
                        Text("Alarmas programadas")
                        Spacer()
                        Text("\(alarmStore.alarms.count)")
                            .foregroundStyle(.secondary)
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
                let cleanName = alarmName.trimmingCharacters(in: .whitespacesAndNewlines)

                try await alarmStore.scheduleAlarm(
                    name: cleanName,
                    fireDate: selectedDate
                )

                alertTitle = "Alarma programada"
                alertMessage = "\(cleanName) sonará el \(selectedDate.formatted(date: .long, time: .shortened))."
                alarmName = ""
                selectedDate = Date().addingTimeInterval(5 * 60)
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

    private var dateText: String {
        if Calendar.current.isDateInToday(alarm.fireDate) {
            return "Hoy · \(alarm.fireDate.formatted(date: .omitted, time: .shortened))"
        }

        return alarm.fireDate.formatted(date: .abbreviated, time: .shortened)
    }

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "alarm.fill")
                .font(.title2)
                .foregroundStyle(.blue)

            VStack(alignment: .leading, spacing: 3) {
                Text(alarm.name)
                    .font(.headline)

                Text(dateText)
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
