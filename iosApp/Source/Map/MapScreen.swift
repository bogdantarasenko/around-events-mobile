import AroundKit
import SwiftUI

/// The map tab as in Apple's own maps: the map edge to edge, glass controls on the right, a "List" button over
/// the tab bar, and the events in a system sheet — all of them, or the tapped place's.
struct MapScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<MapViewModel>(Deps.shared.getMapViewModel())
    @State private var state: MapUiState?
    @State private var fitToken = 0
    @State private var locateToken = 0
    @State private var showsList = false

    var body: some View {
        let state = state ?? store.instance.state.value
        MapLibreMap(
            places: MapPlace.group(state.events),
            padding: UIEdgeInsets(top: 120, left: 50, bottom: 140, right: 70),
            fitToken: fitToken,
            locateToken: locateToken
        ) { ids in
            store.instance.select(ids: ids.map { KotlinLong(value: $0) })
            showsList = true
        }
        .ignoresSafeArea(edges: .top)
        .overlay(alignment: .topTrailing) { controls.padding(.trailing, 16).padding(.top, 8) }
        .overlay(alignment: .bottom) { listButton(state).padding(.bottom, 12) }
        .sheet(isPresented: $showsList, onDismiss: { store.instance.clearSelection() }) {
            MapEventsSheet(state: state) { id in
                showsList = false
                path.append(.eventDetail(id))
            }
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled(upThrough: .medium))
        }
        .toolbar(path.isEmpty ? .hidden : .visible, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .sensoryFeedback(.selection, trigger: state.isSelection)
        .onAppear {
            #if DEBUG
            // `-AroundMapList YES` opens the list at launch, to check it in the simulator.
            if UserDefaults.standard.bool(forKey: "AroundMapList") { showsList = true }
            #endif
        }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    // One glass column, as the map controls in Apple's maps.
    private var controls: some View {
        VStack(spacing: 0) {
            Button { locateToken += 1 } label: {
                Image(systemName: "location").frame(width: 44, height: 44)
            }
            .accessibilityLabel(strings.mapMyLocation)
            Divider().frame(width: 28)
            Button {
                store.instance.clearSelection()
                fitToken += 1
            } label: {
                Image(systemName: "arrow.up.left.and.arrow.down.right").frame(width: 44, height: 44)
            }
            .accessibilityLabel(strings.mapShowAll)
        }
        .font(.body.weight(.medium))
        .foregroundStyle(Palette.ink)
        .glassBackground(in: RoundedRectangle(cornerRadius: 22))
    }

    private func listButton(_ state: MapUiState) -> some View {
        Button { showsList = true } label: {
            Label("\(strings.mapList) · \(state.events.count)", systemImage: "list.bullet")
                .font(.subheadline.weight(.semibold))
                .padding(.horizontal, 6)
                .padding(.vertical, 4)
        }
        .glassButtonStyle()
        .buttonBorderShape(.capsule)
        .disabled(state.events.isEmpty)
    }
}
