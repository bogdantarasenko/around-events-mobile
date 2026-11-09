import SwiftUI

/// An emoji and a tint per category: the chips on the home screen and the stand-in when an event has no poster.
enum CategoryStyle {
    static func emoji(_ key: String?) -> String {
        switch key {
        case "concert", "music": "🎵"
        case "standup": "🎤"
        case "theatre": "🎭"
        case "party": "🪩"
        case "festival": "🎪"
        case "exhibition": "🖼️"
        case "kids": "🧸"
        case "market": "🛍️"
        case "food": "🍽️"
        case "cinema": "🎬"
        case "lecture", "learning": "🎓"
        case "sport": "⚽️"
        case "games": "🎲"
        case "creative": "🎨"
        default: "✨"
        }
    }

    static func tint(_ key: String?) -> Color {
        switch key {
        case "concert", "music", "standup": .purple
        case "theatre", "cinema": .red
        case "party", "festival": .pink
        case "exhibition", "creative": .orange
        case "kids", "games": .yellow
        case "market", "food": .brown
        case "lecture", "learning": .blue
        case "sport": .green
        default: .teal
        }
    }
}
