//
//  TiviApp.swift
//  Tivi
//
//  Created by Chris Banes on 28/06/2023.
//

import AppAuth
import FirebaseAnalytics
import FirebaseCore
import SwiftUI
import TiviKt

class AppDelegate: UIResponder, UIApplicationDelegate {
    // property of the app's AppDelegate
    var currentAuthorizationFlow: OIDExternalUserAgentSession?

    lazy var applicationComponent: IosApplicationComponent = createApplicationComponent(
        appDelegate: self
    )

    func application(
        _: UIApplication,
        didFinishLaunchingWithOptions _: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        if !(FirebaseOptions.defaultOptions()?.apiKey?.isEmpty ?? true) {
            FirebaseApp.configure()
        }
        applicationComponent.initializers.initialize()
        return true
    }

    func application(
        _: UIApplication,
        open url: URL,
        options _: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        if let authorizationFlow = currentAuthorizationFlow,
           authorizationFlow.resumeExternalUserAgentFlow(with: url) {
            currentAuthorizationFlow = nil
            return true
        }

        return false
    }
}

@main
struct TiviApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            let uiComponent = createHomeUiControllerComponent(
                applicationComponent: delegate.applicationComponent
            )
            ContentView(component: uiComponent)
        }
    }
}

private func createApplicationComponent(
    appDelegate: AppDelegate
) -> IosApplicationComponent {
    return IosApplicationComponent.companion.create(
        analyticsProvider: { FirebaseAnalytics() },
        traktRefreshTokenActionProvider: { traktOAuthInfo in
            IosTraktRefreshTokenAction(traktOAuthInfo: traktOAuthInfo)
        },
        traktLoginActionProvider: { traktOAuthInfo in
            IosTraktLoginAction(
                appDelegate: appDelegate,
                uiViewController: {
                    UIApplication.topViewController()!
                },
                traktOAuthInfo: traktOAuthInfo
            )
        }
    )
}

extension UIApplication {
    private class func keyWindowCompat() -> UIWindow? {
        return UIApplication
            .shared
            .connectedScenes
            .flatMap { ($0 as? UIWindowScene)?.windows ?? [] }
            .last { $0.isKeyWindow }
    }

    class func topViewController(
        base: UIViewController? = UIApplication.keyWindowCompat()?.rootViewController
    ) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }

        if let tab = base as? UITabBarController {
            let moreNavigationController = tab.moreNavigationController

            if let top = moreNavigationController.topViewController, top.view.window != nil {
                return topViewController(base: top)
            } else if let selected = tab.selectedViewController {
                return topViewController(base: selected)
            }
        }

        if let presented = base?.presentedViewController {
            return topViewController(base: presented)
        }

        return base
    }
}

private func createHomeUiControllerComponent(
    applicationComponent: IosApplicationComponent
) -> HomeUiControllerComponent {
    return HomeUiControllerComponent.companion.create(
        applicationComponent: applicationComponent
    )
}

class FirebaseAnalytics: TiviAnalytics {
    func trackScreenView(name: String, arguments: [String: Any]?) {
        var params = [AnalyticsParameterScreenName: name]
        arguments?.forEach { key, value in
            params[key] = "screen_arg_\(value)"
        }

        Analytics.logEvent(AnalyticsEventSelectContent, parameters: params)
    }
}
