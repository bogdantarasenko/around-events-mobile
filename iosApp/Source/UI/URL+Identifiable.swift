import Foundation

/// Lets a URL drive `.sheet(item:)`.
extension URL: @retroactive Identifiable {
    public var id: String { absoluteString }
}
