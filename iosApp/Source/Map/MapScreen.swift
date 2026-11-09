import AroundKit
import SwiftUI

/// The map tab as in Apple's own maps: the map edge to edge under the tab bar, a time filter on top, glass controls
/// on the right, "33 of 86 on the map" at the bottom, and the events in a system sheet.
struct MapScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<MapViewModel>(Deps.shared.getMapViewModel())
    @State private var state: MapUiState?
    @State private var fitToken = 0
    @State private var locateToken = 0
    @State private var showsList = false
    // Set while an event opened from the sheet is on screen: coming back reopens the sheet where it was.
    @State private var returnsToList = false

    var body: some View {
        let state = state ?? store.instance.state.value
        ZStack {
            MapLibreMap(
                places: MapPlace.group(state.events),
                selected: Set(state.selected.map(\.int64Value)),
                padding: UIEdgeInsets(top: 140, left: 50, bottom: 200, right: 70),
                fitToken: fitToken,
                locateToken: locateToken,
                onSelect: { ids in
                    store.instance.select(ids: ids.map { KotlinLong(value: $0) })
                    showsList = true
                },
                onClear: { store.instance.clearSelection() }
            )
            .ignoresSafeArea()

            VStack(spacing: 12) {
                periodPicker(state)
                HStack {
                    Spacer()
                    controls
                }
                Spacer()
                bottomBar(state)
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 12)
        }
        .sheet(isPresented: $showsList, onDismiss: {
            if !returnsToList { store.instance.clearSelection() }
        }) {
            MapEventsSheet(state: state) { id in
                returnsToList = true
                showsList = false
                path.append(.eventDetail(id))
            }
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled(upThrough: .medium))
        }
        .onAppear {
            if returnsToList {
                returnsToList = false
                showsList = true
            }
            #if DEBUG
            // `-AroundMapList YES` opens the list at launch, to check it in the simulator.
            if UserDefaults.standard.bool(forKey: "AroundMapList") { showsList = true }
            #endif
        }
        .toolbar(path.isEmpty ? .hidden : .visible, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .sensoryFeedback(.selection, trigger: state.selected)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func periodPicker(_ state: MapUiState) -> some View {
        Picker(strings.mapTitle, selection: Binding(get: { state.period }, set: { store.instance.selectPeriod(value: $0) })) {
            Text(strings.feedToday).tag(Period.today)
            Text(strings.feedTomorrow).tag(Period.tomorrow)
            Text(strings.periodWeekend).tag(Period.weekend)
            Text(strings.feedAll).tag(Period.all)
        }
        .pickerStyle(.segmented)
        .padding(4)
        .glassBackground(in: Capsule())
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

    @ViewBuilder
    private func bottomBar(_ state: MapUiState) -> some View {
        if state.events.isEmpty {
            Text(strings.mapEmpty)
                .font(.subheadline.weight(.medium))
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .glassBackground(in: Capsule())
        } else {
            Button { showsList = true } label: {
                Label(strings.mapOnMap("\(state.events.count)", "\(state.total)"), systemImage: "list.bullet")
                    .font(.subheadline.weight(.semibold))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 4)
            }
            .glassButtonStyle()
            .buttonBorderShape(.capsule)
        }
    }
}
