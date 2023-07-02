//
//  TiviApp.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import SwiftUI
import TiviKt
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        if FirebaseOptions.defaultOptions()?.apiKey != nil {
            FirebaseApp.configure()
        }
        return true
    }
}

@main
struct TiviApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    let applicationComponent = IosApplicationComponent.companion.create()

    init() {
        applicationComponent.initializers.initialize()
    }

    var body: some Scene {
        WindowGroup {
            let uiComponent = HomeUiControllerComponent.companion.create(
                applicationComponent: applicationComponent
            )
            ContentView(component: uiComponent)
        }
    }
}
