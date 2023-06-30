//
//  ContentView.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import SwiftUI
import TiviKt

struct ContentView: View {
    let component: HomeUiControllerComponent

    init(component: HomeUiControllerComponent) {
        self.component = component
    }

    var body: some View {
        ComposeView(component: self.component)
            .ignoresSafeArea(.all, edges: .bottom) // Compose has own keyboard handler
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let component: HomeUiControllerComponent

    init(component: HomeUiControllerComponent) {
        self.component = component
    }

    func makeUIViewController(context: Context) -> UIViewController {
        component.uiViewController {
            // onRootPop
        } onOpenSettings: {
            // no-op
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
