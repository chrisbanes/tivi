//
//  TiviApp.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import SwiftUI
import TiviKt
import FirebaseAnalytics
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        if !(FirebaseOptions.defaultOptions()?.apiKey?.isEmpty ?? true) {
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
        applicationComponent.analyticsProvider = { FirebaseAnalytics() }
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

class FirebaseAnalytics: TiviAnalytics {
    func trackScreenView(name: String, arguments: [String : Any]?) {
        var params = [AnalyticsParameterScreenName: name]
        arguments?.forEach { (key, value) in
            params[key] = "screen_arg_\(value)"
        }
        
        Analytics.logEvent(AnalyticsEventSelectContent, parameters: params)
    }
}
