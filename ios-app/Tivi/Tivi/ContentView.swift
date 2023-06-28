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
    
    init(applicationComponent: IosApplicationComponent) {
        self.component = HomeUiControllerComponent.companion.create(applicationComponent: applicationComponent)
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
        HomeKt.HomeViewController(
            onRootPop: {
                // todo
            },
            onOpenSettings: {
                // todo
            },
            imageLoader: component.imageLoader,
            tiviDateFormatter: component.tiviDateFormatter,
            tiviTextCreator: component.textCreator,
            circuitConfig: component.circuitConfig,
            analytics: component.analytics,
            preferences: component.preferences
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
