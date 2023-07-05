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
    
    func invoke() async throws -> AuthState? {
        let request = OIDAuthorizationRequest(
            configuration: configuration,
            clientId: traktOAuthInfo.clientId,
            clientSecret: traktOAuthInfo.clientSecret,
            scopes: [],
            redirectURL: URL(string: traktOAuthInfo.redirectUri)!,
            responseType: OIDResponseTypeCode,
            additionalParameters: nil
        )
        return await login(request: request)
    }
    
    @MainActor private func login(request: OIDAuthorizationRequest) async -> AuthState? {
        return await withCheckedContinuation { continuation in
            self.appDelegate.currentAuthorizationFlow = OIDAuthState.authState(byPresenting: request, presenting: self.uiViewController()) { authState, error in
                if let authState = authState {
                    let tiviAuthState = SimpleAuthState(
                        accessToken: authState.lastTokenResponse?.accessToken ?? "",
                        refreshToken: authState.lastTokenResponse?.refreshToken ?? ""
                    )
                    continuation.resume(returning: tiviAuthState)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }
    
    func register() {
        // not required on iOS
    }
}
