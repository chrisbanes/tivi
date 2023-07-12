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

class IosTraktRefreshTokenAction: TraktRefreshTokenAction {
    private let traktOAuthInfo: TraktOAuthInfo

    init(traktOAuthInfo: TraktOAuthInfo) {
        self.traktOAuthInfo = traktOAuthInfo
    }

    func invoke(state: AuthState) async throws -> AuthState? {
        let request = OIDTokenRequest(
            configuration: configuration,
            grantType: OIDGrantTypeRefreshToken,
            authorizationCode: nil,
            redirectURL: nil,
            clientID: traktOAuthInfo.clientId,
            clientSecret: traktOAuthInfo.clientSecret,
            scope: nil,
            refreshToken: state.refreshToken,
            codeVerifier: nil,
            additionalParameters: nil)

        return await refresh(request: request)
    }

    @MainActor private func refresh(request: OIDTokenRequest) async -> AuthState? {
        return await withCheckedContinuation { continuation in
            OIDAuthorizationService.perform(request) { response, _ in
                if let response = response {
                    let authState = SimpleAuthState(
                        accessToken: response.accessToken ?? "",
                        refreshToken: response.refreshToken ?? ""
                    )
                    continuation.resume(returning: authState)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }
}

class IosTraktLoginAction: TraktLoginAction {
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
            self.appDelegate.currentAuthorizationFlow = OIDAuthState.authState(
                byPresenting: request,
                presenting: self.uiViewController()
            ) { authState, _ in
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
}
