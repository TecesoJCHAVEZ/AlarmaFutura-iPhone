import Foundation

struct SavedAlarm: Identifiable, Codable, Hashable {
    let id: UUID
    let name: String
    let fireDate: Date
}
