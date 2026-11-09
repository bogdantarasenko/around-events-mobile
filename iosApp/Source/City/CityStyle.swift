import SwiftUI

/// A colour per city, by its place in the list: the backend has no city photos, so colours tell them apart.
enum CityStyle {
    static func colors(_ index: Int) -> [Color] {
        let palette: [[Color]] = [[.indigo, .purple], [.teal, .blue], [.orange, .pink], [.green, .teal]]
        return palette[index % palette.count]
    }

    static func color(_ index: Int) -> Color { colors(index)[0] }
}
