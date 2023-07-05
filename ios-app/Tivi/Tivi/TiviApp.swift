//
//  TiviApp.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import AppAuth
import SwiftUI
import TiviKt
import FirebaseAnalytics
import FirebaseCore

class AppDelegate: UIResponder, UIApplicationDelegate {
    // property of the app's AppDelegate
    var currentAuthorizationFlow: OIDExternalUserAgentSession?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        if !(FirebaseOptions.defaultOptions()?.apiKey?.isEmpty ?? true) {
            FirebaseApp.configure()
        }
        return true
    }
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        if let authorizationFlow = self.currentAuthorizationFlow,
           authorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            return true
        }
        
        return false
    }
}

@main
struct TiviApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    let applicationComponent: IosApplicationComponent
    
    init() {
        applicationComponent = createApplicationComponent()
        applicationComponent.initializers.initialize()
    }
    
    var body: some Scene {
        WindowGroup {
            let uiComponent = createHomeUiControllerComponent(
                applicationComponent: applicationComponent,
                appDelegate: delegate
            )
            ContentView(component: uiComponent)
        }
    }
}

private func createApplicationComponent() -> IosApplicationComponent {
    return IosApplicationComponent.companion.create(
        analyticsProvider: { FirebaseAnalytics() },
        refreshTraktTokensInteractorProvider: { traktOAuthInfo in
            IosRefreshTraktTokensInteractor(traktOAuthInfo: traktOAuthInfo)
        }
    )
}

private func createHomeUiControllerComponent(
    applicationComponent: IosApplicationComponent,
    appDelegate: AppDelegate
) -> HomeUiControllerComponent {
    return HomeUiControllerComponent.companion.create(
        applicationComponent: applicationComponent,
        loginToTraktInteractorProvider: { traktOAuthInfo, uiViewController in
            IosLoginToTraktInteractor(
                appDelegate: appDelegate,
                uiViewController: uiViewController,
                traktOAuthInfo: traktOAuthInfo
            )
        }
    )
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
