import Foundation

/// Links as people typed them: `t.me/name` without a scheme is common.
enum Links {
    static func url(_ raw: String?) -> URL? {
        guard let raw = raw?.trimmingCharacters(in: .whitespaces), !raw.isEmpty else { return nil }
        return URL(string: raw.contains("://") ? raw : "https://" + raw)
    }
}
