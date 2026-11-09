import AroundKit
import Foundation

/// Owns a Kotlin `ViewModel` for the lifetime of a SwiftUI view and clears it on deinit,
/// which cancels its `viewModelScope`. Held with `@StateObject` so the autoclosure runs once per view identity.
final class VMStoreOwner<VM: ViewModel>: ObservableObject, ViewModelStoreOwner {
    let viewModelStore = ViewModelStore()

    private let key = String(describing: VM.self)

    init(_ viewModel: VM) {
        viewModelStore.put(key: key, viewModel: viewModel)
    }

    var instance: VM {
        // swiftlint:disable:next force_cast
        viewModelStore.get(key: key) as! VM
    }

    deinit {
        viewModelStore.clear()
    }
}
