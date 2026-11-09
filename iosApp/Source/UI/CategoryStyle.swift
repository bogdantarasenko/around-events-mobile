import SwiftUI

/// An icon and a tint per category: the chips on the home screen and the stand-in when an event has no poster.
enum CategoryStyle {
    static func symbol(_ key: String?) -> String {
        switch key {
        case "concert", "music": "music.note"
        case "standup": "music.mic"
        case "theatre": "theatermasks"
        case "party": "party.popper"
        case "festival": "tent"
        case "exhibition": "photo.artframe"
        case "kids": "figure.and.child.holdinghands"
        case "market": "bag"
        case "food": "fork.knife"
        case "cinema": "film"
        case "lecture", "learning": "graduationcap"
        case "sport": "figure.run"
        case "games": "dice"
        case "creative": "paintpalette"
        default: "sparkles"
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
