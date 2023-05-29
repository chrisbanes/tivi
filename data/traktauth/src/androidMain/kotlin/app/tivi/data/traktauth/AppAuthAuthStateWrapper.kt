// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

internal class AppAuthAuthStateWrapper(
    val authState: net.openid.appauth.AuthState,
) : AuthState {
    constructor(json: String) : this(net.openid.appauth.AuthState.jsonDeserialize(json))

    override val accessToken: String get() = authState.accessToken.orEmpty()
    override val refreshToken: String get() = authState.refreshToken.orEmpty()
    override val isAuthorized: Boolean get() = authState.isAuthorized
    override fun serializeToJson(): String = authState.jsonSerializeString()
}
