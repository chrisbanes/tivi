//
//  Auth.kt.swift
//  Tivi
//
//  Created by Chris Banes on 05/07/2023.
//

import AppAuth
import Foundation
import TiviKt

private let configuration = OIDServiceConfiguration(
    authorizationEndpoint: URL(string: "https://trakt.tv/oauth/authorize")!,
    tokenEndpoint: URL(string: "https://trakt.tv/oauth/token")!
)

class IosRefreshTraktTokensInteractor: RefreshTraktTokensInteractor {
    private let traktOAuthInfo: TraktOAuthInfo
    
    init(traktOAuthInfo: TraktOAuthInfo) {
        self.traktOAuthInfo = traktOAuthInfo
    }
    
    func invoke() async throws -> AuthState? {
        // TODO
        return nil
    }
}

class IosLoginToTraktInteractor: LoginToTraktInteractor {
    private let appDelegate: AppDelegate
    private let uiViewController: () -> UIViewController
    private let traktOAuthInfo: TraktOAuthInfo
    
    init(
        appDelegate: AppDelegate,
        uiViewController: @escaping () -> UIViewController,
        traktOAuthInfo: TraktOAuthInfo
    ) {
        self.appDelegate = appDelegate
        self.uiViewController = uiViewController
        self.traktOAuthInfo = traktOAuthInfo
    }
    
    func launch() {
        let request = OIDAuthorizationRequest(
            configuration: configuration,
            clientId: traktOAuthInfo.clientId,
            clientSecret: traktOAuthInfo.clientSecret,
            scopes: [],
            redirectURL: URL(string: traktOAuthInfo.redirectUri)!,
            responseType: OIDResponseTypeCode,
            additionalParameters: nil
        )
        
        // performs authentication request
        print("Initiating authorization request with scope: \(request.scope ?? "nil")")
        
        appDelegate.currentAuthorizationFlow = OIDAuthState.authState(byPresenting: request, presenting: uiViewController()) { authState, error in
            if let authState = authState {
                // TODO do something with authState
                print("Got authorization tokens. Access token: " +
                      "\(authState.lastTokenResponse?.accessToken ?? "nil")")
            } else {
                print("Authorization error: \(error?.localizedDescription ?? "Unknown error")")
            }
        }
    }
    
    func register() {
        // not required on iOS
    }
}
