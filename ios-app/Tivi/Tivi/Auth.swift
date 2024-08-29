//
//  Auth.kt.swift
//  Tivi
//
//  Created by Chris Banes on 05/07/2023.
//

import AppAuth
import Foundation
import TiviKt
import os

private let configuration = OIDServiceConfiguration(
    authorizationEndpoint: URL(string: "https://trakt.tv/oauth/authorize")!,
    tokenEndpoint: URL(string: "https://trakt.tv/oauth/token")!
)

class IosTraktRefreshTokenAction: TraktRefreshTokenAction {
    private let traktOAuthInfo: TraktOAuthInfo
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier!, category: "network")

    init(traktOAuthInfo: TraktOAuthInfo) {
        self.traktOAuthInfo = traktOAuthInfo
    }

    func invoke(state: AuthState) async throws -> AuthState? {
        logger.info("IosTraktRefreshTokenAction.invoke()")

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

        do {
            let result = try await refresh(request: request)
            logger.info("IosTraktRefreshTokenAction. invoke result")
            return result
        } catch {
            logger.error("IosTraktRefreshTokenAction. Error: \(error)")
            throw error
        }
    }

    @MainActor private func refresh(request: OIDTokenRequest) async throws -> AuthState? {
        return try await withCheckedThrowingContinuation { continuation in
            OIDAuthorizationService.perform(request) { response, error in
                if let response = response {
                    let authState = SimpleAuthState(
                        accessToken: response.accessToken ?? "",
                        refreshToken: response.refreshToken ?? ""
                    )
                    continuation.resume(returning: authState)
                } else if let error = error {
                    continuation.resume(throwing: error)
                }
            }
        }
    }
}

class IosTraktLoginAction: TraktLoginAction {
    private let appDelegate: AppDelegate
    private let uiViewController: () -> UIViewController
    private let traktOAuthInfo: TraktOAuthInfo

    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier!, category: "network")

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
        logger.info("IosTraktLoginAction.invoke()")

        let request = OIDAuthorizationRequest(
            configuration: configuration,
            clientId: traktOAuthInfo.clientId,
            clientSecret: traktOAuthInfo.clientSecret,
            scopes: [],
            redirectURL: URL(string: traktOAuthInfo.redirectUri)!,
            responseType: OIDResponseTypeCode,
            additionalParameters: nil
        )

        do {
            let result = try await login(request: request)
            logger.info("IosTraktLoginAction. invoke result")
            return result
        } catch {
            logger.error("IosTraktLoginAction. Error: \(error)")
            throw error
        }
    }

    @MainActor private func login(request: OIDAuthorizationRequest) async throws -> AuthState? {
        return try await withCheckedThrowingContinuation { continuation in
            self.appDelegate.currentAuthorizationFlow = OIDAuthState.authState(
                byPresenting: request,
                presenting: self.uiViewController(),
                prefersEphemeralSession: true
            ) { authState, error in
                if let authState = authState {
                    let tiviAuthState = SimpleAuthState(
                        accessToken: authState.lastTokenResponse?.accessToken ?? "",
                        refreshToken: authState.lastTokenResponse?.refreshToken ?? ""
                    )
                    continuation.resume(returning: tiviAuthState)
                } else if let error = error {
                    continuation.resume(throwing: error)
                }
            }
        }
    }
}
