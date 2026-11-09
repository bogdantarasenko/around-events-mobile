import AroundKit
import MapLibre
import SwiftUI

/// The style layers that draw the events: clusters with a count, a dot per place in its category's colour, a ring
/// around the selected place, and the place's label from street zoom on.
enum MapLayers {
    static let source = "events"
    static let clusters = "events-clusters"
    static let points = "events-points"
    static let selection = "events-selected"
    private static let clusterCount = "events-cluster-count"
    private static let labels = "events-labels"
    // Present in both OpenFreeMap styles; a font the style's glyph server lacks draws no text at all.
    private static let font = ["Noto Sans Regular"]

    static func feature(_ place: MapPlace) -> MLNPointFeature {
        let feature = MLNPointFeature()
        feature.coordinate = place.coordinate
        let first = place.events[0]
        feature.attributes = [
            "place": place.id,
            "ids": place.events.map { String($0.id) }.joined(separator: ","),
            "category": first.category ?? "other",
            "label": place.label,
        ]
        return feature
    }

    static func add(to style: MLNStyle, source: MLNShapeSource, dark: Bool, interactive: Bool) {
        let ink = UIColor(dark ? .white : Color(white: 0.08))
        let halo = UIColor(dark ? Color(white: 0.05) : .white)
        let isCluster = NSPredicate(format: "cluster == YES")
        let isPlace = NSPredicate(format: "cluster != YES")

        let clusters = MLNCircleStyleLayer(identifier: Self.clusters, source: source)
        clusters.predicate = isCluster
        clusters.circleColor = NSExpression(forConstantValue: ink)
        clusters.circleRadius = NSExpression(forConstantValue: 17)
        clusters.circleStrokeColor = NSExpression(forConstantValue: halo)
        clusters.circleStrokeWidth = NSExpression(forConstantValue: 3)
        style.addLayer(clusters)

        let count = MLNSymbolStyleLayer(identifier: Self.clusterCount, source: source)
        count.predicate = isCluster
        count.text = NSExpression(format: "CAST(point_count, 'NSString')")
        count.textFontNames = NSExpression(forConstantValue: Self.font)
        count.textFontSize = NSExpression(forConstantValue: 14)
        count.textColor = NSExpression(forConstantValue: halo)
        count.textAllowsOverlap = NSExpression(forConstantValue: true)
        style.addLayer(count)

        let selection = MLNCircleStyleLayer(identifier: Self.selection, source: source)
        selection.predicate = NSPredicate(format: "place IN %@", [String]())
        selection.circleColor = NSExpression(forConstantValue: UIColor.clear)
        selection.circleRadius = NSExpression(forConstantValue: 15)
        selection.circleStrokeColor = NSExpression(forConstantValue: ink)
        selection.circleStrokeWidth = NSExpression(forConstantValue: 3)
        style.addLayer(selection)

        let points = MLNCircleStyleLayer(identifier: Self.points, source: source)
        points.predicate = isPlace
        points.circleColor = NSExpression(
            forMLNMatchingKey: NSExpression(forKeyPath: "category"),
            in: Dictionary(uniqueKeysWithValues: CategoryStyle.keys.map {
                (NSExpression(forConstantValue: $0), NSExpression(forConstantValue: UIColor(CategoryStyle.tint($0))))
            }),
            default: NSExpression(forConstantValue: UIColor(CategoryStyle.tint(nil)))
        )
        points.circleRadius = NSExpression(forConstantValue: interactive ? 9 : 10)
        points.circleStrokeColor = NSExpression(forConstantValue: halo)
        points.circleStrokeWidth = NSExpression(forConstantValue: 2.5)
        style.addLayer(points)

        guard interactive else { return }
        let labels = MLNSymbolStyleLayer(identifier: Self.labels, source: source)
        labels.predicate = isPlace
        labels.minimumZoomLevel = 13
        labels.text = NSExpression(forKeyPath: "label")
        labels.textFontNames = NSExpression(forConstantValue: Self.font)
        labels.textFontSize = NSExpression(forConstantValue: 12)
        labels.textColor = NSExpression(forConstantValue: ink)
        labels.textHaloColor = NSExpression(forConstantValue: halo)
        labels.textHaloWidth = NSExpression(forConstantValue: 1.5)
        labels.textAnchor = NSExpression(forConstantValue: "top")
        labels.textOffset = NSExpression(forConstantValue: NSValue(cgVector: CGVector(dx: 0, dy: 1.1)))
        labels.maximumTextWidth = NSExpression(forConstantValue: 9)
        labels.textOptional = NSExpression(forConstantValue: true)
        style.addLayer(labels)
    }
}
