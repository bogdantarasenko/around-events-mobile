/// Screens pushed on the navigation stack.
enum AppRoute: Hashable {
    case eventDetail(Int64)
    /// The city's listing; with a category, that category's page.
    case events(category: String?)
    case organizer(slug: String)
    case map
    case search
    case settings
}
