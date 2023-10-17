// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.util.AppCoroutineDispatchers
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.set
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrService

@OptIn(
  ExperimentalSettingsImplementation::class,
  ExperimentalForeignApi::class,
  ExperimentalSettingsApi::class,
)
@Inject
class IosAuthStore(
  private val applicationInfo: ApplicationInfo,
  private val dispatchers: AppCoroutineDispatchers,
) : AuthStore {
  private val keyChainSettings by lazy {
    KeychainSettings(
      kSecAttrService to CFBridgingRetain("${applicationInfo.packageName}.auth"),
      kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock,
    )
  }

  override suspend fun get(): AuthState? = withContext(dispatchers.io) {
    val accessToken = keyChainSettings.getStringOrNull(KEY_ACCESS_TOKEN)
    val refreshToken = keyChainSettings.getStringOrNull(KEY_REFRESH_TOKEN)
    if (accessToken != null && refreshToken != null) {
      SimpleAuthState(accessToken, refreshToken)
    } else {
      null
    }
  }

  override suspend fun save(state: AuthState) = withContext(dispatchers.io) {
    keyChainSettings[KEY_ACCESS_TOKEN] = state.accessToken
    keyChainSettings[KEY_REFRESH_TOKEN] = state.refreshToken
  }

  override suspend fun clear() = withContext(dispatchers.io) {
    keyChainSettings.remove(KEY_ACCESS_TOKEN)
    keyChainSettings.remove(KEY_REFRESH_TOKEN)
  }
}

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
