// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

interface AuthState {
  val accessToken: String
  val refreshToken: String
  val isAuthorized: Boolean
  fun serializeToJson(): String

  companion object {
    val Empty: AuthState = object : AuthState {
      override val accessToken: String = ""
      override val refreshToken: String = ""
      override val isAuthorized: Boolean = false
      override fun serializeToJson(): String = "{}"
    }
  }
}

data class SimpleAuthState(
  override val accessToken: String,
  override val refreshToken: String,
) : AuthState {
  override val isAuthorized: Boolean
    get() = accessToken.isNotEmpty() && refreshToken.isNotEmpty()

  override fun serializeToJson(): String = error("serializeToJson not implemented")
}
