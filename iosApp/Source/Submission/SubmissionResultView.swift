import AroundKit
import SwiftUI

struct SubmissionResultView: View {
    let result: FormResult
    let kind: FormKind
    let onDone: () -> Void
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 60))
                .foregroundStyle(.tint)
            Text(title).font(.title2.weight(.semibold))
            Text(text).multilineTextAlignment(.center)
            if !result.saved, result.receipt?.status == "review" {
                steps
            }
            if let post = result.receipt?.postUrl, let url = URL(string: post) {
                Button(strings.openPost) { openURL(url) }.buttonStyle(.bordered)
            }
            Button {
                onDone()
            } label: {
                Text(kind == .edit ? strings.back : strings.toMine).frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            Spacer()
        }
        .padding(24)
        .navigationBarBackButtonHidden()
    }

    private var title: String {
        if result.saved { return strings.savedTitle }
        return result.isDuplicate ? strings.doneDuplicate : strings.doneAccepted
    }

    // The server explains a duplicate or an auto-published event in its own words.
    private var text: String {
        result.saved ? strings.savedReview : (result.receipt?.message ?? strings.reviewTakes)
    }

    private var steps: some View {
        VStack(alignment: .leading, spacing: 10) {
            step(1, strings.timelineReview, strings.timelineReviewHint)
            step(2, strings.timelineAnswer, strings.timelineAnswerHint)
            step(3, strings.timelinePost, strings.timelinePostHint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func step(_ number: Int, _ title: String, _ hint: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("\(number). \(title)").font(.subheadline.weight(.semibold))
            Text(hint).font(.footnote).foregroundStyle(.secondary)
        }
    }
}
