import AroundKit
import MapKit
import SwiftUI

/// The city's events on a map: posters as pins, the list in a sheet below.
struct MapScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<MapViewModel>(Deps.shared.getMapViewModel())
    @State private var state: MapUiState?
    @State private var camera: MapCameraPosition = .automatic
    @State private var showsSheet = true

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
        .mapControls { MapCompass() }
        .overlay(alignment: .topLeading) {
            CircleButton(systemImage: "chevron.left", label: strings.back) {
                if !path.isEmpty { path.removeLast() }
            }
            .padding()
        }
        .overlay(alignment: .topTrailing) {
            CircleButton(systemImage: "location.viewfinder", label: strings.mapTitle) {
                store.instance.clearSelection()
                withAnimation { camera = .automatic }
            }
            .padding()
        }
        .toolbar(.hidden, for: .navigationBar)
        .sheet(isPresented: $showsSheet) {
            MapSheet(state: state, onClose: { store.instance.clearSelection() }) { id in
                showsSheet = false
                path.append(.eventDetail(id))
            }
            .presentationDetents([.fraction(0.35), .large])
            .presentationBackgroundInteraction(.enabled(upThrough: .fraction(0.35)))
            .interactiveDismissDisabled()
        }
        .onAppear { showsSheet = true }
        .onDisappear { showsSheet = false }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }
}
