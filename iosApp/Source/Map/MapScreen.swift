import AroundKit
import SwiftUI

/// The map tab: posters as pins, a locate button, and the floating card with the events below.
struct MapScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<MapViewModel>(Deps.shared.getMapViewModel())
    @State private var state: MapUiState?
    @State private var fitToken = 0
    @State private var expanded = false

    var body: some View {
        let state = state ?? store.instance.state.value
        MapLibreMap(
            places: MapPlace.group(state.events),
            padding: UIEdgeInsets(top: 90, left: 50, bottom: 420, right: 50),
            fitToken: fitToken
        ) { ids in
            store.instance.select(ids: ids.map { KotlinLong(value: $0) })
        }
        .ignoresSafeArea()
        .overlay(alignment: .bottom) {
            VStack(alignment: .trailing, spacing: 12) {
                Button {
                    store.instance.clearSelection()
                    fitToken += 1
                } label: {
                    Image(systemName: "location.north.line").font(.body.weight(.semibold)).frame(width: 44, height: 44)
                }
                .glassButtonStyle()
                .buttonBorderShape(.circle)
                .accessibilityLabel(strings.mapTitle)
                .padding(.trailing, 22)
                MapSheet(state: state, expanded: $expanded, onClose: { store.instance.clearSelection() }) { id in
                    path.append(.eventDetail(id))
                }
            }
            .padding(.bottom, 8)
        }
        .toolbar(path.isEmpty ? .hidden : .visible, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }
}
