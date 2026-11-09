import SwiftUI
import UIKit

/// The design's greys and accents (Figma `Luma/Gray/*`, `Luma/Color/*`), with dark-mode counterparts.
enum Palette {
    static let ink = adaptive(0x141414, dark: 0xF2F2F2)
    static let secondary = adaptive(0x8C8C8C, dark: 0x9A9A9A)
    static let tertiary = adaptive(0xA6A6A6, dark: 0x7A7A7A)
    static let weekday = adaptive(0x9A9A9A, dark: 0x8A8A8A)
    static let slash = adaptive(0xD4D4D4, dark: 0x4A4A4A)
    static let chevron = adaptive(0xBDBDBD, dark: 0x5A5A5A)
    static let separator = adaptive(0xE2E2E2, dark: 0x2E2E2E)
    static let chipBorder = adaptive(0xE9E9E9, dark: 0x333333)
    static let chipText = adaptive(0x6B6B6B, dark: 0xB0B0B0)
    static let chipCount = adaptive(0xABABAB, dark: 0x6E6E6E)
    static let price = adaptive(0x3DA92B, dark: 0x5CC94A)
    static let priceBackground = adaptive(0xF4FAF2, dark: 0x1C2A1A)
    static let surface = adaptive(0xFFFFFF, dark: 0x1C1C1E)
    static let tabSelected = adaptive(0xEBEBEB, dark: 0x3A3A3C)
    static let tabInactive = adaptive(0xA8A8A8, dark: 0x8E8E93)

    private static func adaptive(_ light: UInt32, dark: UInt32) -> Color {
        Color(UIColor { $0.userInterfaceStyle == .dark ? color(dark) : color(light) })
    }

    private static func color(_ hex: UInt32) -> UIColor {
        UIColor(
            red: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255,
            alpha: 1
        )
    }
}
