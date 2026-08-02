import SwiftUI

@main
struct AlarmaFuturaApp: App {
    @StateObject private var alarmStore = AlarmStore()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(alarmStore)
        }
    }
}
