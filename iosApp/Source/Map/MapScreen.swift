import AroundKit
import MapKit
import SwiftUI

/// The map tab: posters as pins, a locate button, and the floating card with the events below.
struct MapScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<MapViewModel>(Deps.shared.getMapViewModel())
    @State private var state: MapUiState?
    @State private var camera: MapCameraPosition = .automatic
    @State private var expanded = false

    var body: some View {
        let state = state ?? store.instance.state.value
        Map(position: $camera) {
            ForEach(MapPlace.group(state.events)) { place in
                Annotation(place.caption, coordinate: place.coordinate) {
                    MapPin(place: place)
                        .onTapGesture { store.instance.select(ids: place.events.map { KotlinLong(value: $0.id) }) }
                }
            }
        }
        .mapControls {}
        .overlay(alignment: .topLeading) {
            if !path.isEmpty {
                GlassCircle(systemImage: "chevron.left", label: strings.back) { path.removeLast() }
                    .padding(.leading, 20)
                    .padding(.top, 8)
            }
        }
        .overlay(alignment: .bottom) {
            VStack(alignment: .trailing, spacing: 12) {
                GlassCircle(systemImage: "location.north.line", label: strings.mapTitle) {
                    store.instance.clearSelection()
                    withAnimation { camera = .automatic }
                }
                .padding(.trailing, 22)
                MapSheet(state: state, expanded: $expanded, onClose: { store.instance.clearSelection() }) { id in
                    path.append(.eventDetail(id))
                }
            }
            .padding(.bottom, 78)
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }
}
