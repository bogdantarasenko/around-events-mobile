import AroundKit

/// Screens pushed on the home navigation stack.
enum AppRoute: Hashable {
    case eventDetail(Int64)
    case form(FormKind, Int64)
    case settings
    case organizerEdit
}
