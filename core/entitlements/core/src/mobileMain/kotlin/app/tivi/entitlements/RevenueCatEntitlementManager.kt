// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import app.tivi.app.ApplicationInfo
import app.tivi.app.Platform
import com.revenuecat.purchases.kmp.EntitlementVerificationMode
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.entitlements
import com.revenuecat.purchases.kmp.get
import com.revenuecat.purchases.kmp.isActive
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject

@Inject
class RevenueCatEntitlementManager(
  private val applicationInfo: ApplicationInfo,
) : EntitlementManager {

  override fun setup() {
    val apiKey = when (applicationInfo.platform) {
      Platform.IOS -> BuildConfig.TIVI_REVENUECAT_IOS_API_KEY
      Platform.ANDROID -> BuildConfig.TIVI_REVENUECAT_ANDROID_API_KEY
      else -> null
    }

    if (!apiKey.isNullOrEmpty()) {
      if (applicationInfo.debugBuild) {
        Purchases.logLevel = LogLevel.DEBUG
      }

      Purchases.configure(
        PurchasesConfiguration(apiKey = apiKey) {
          verificationMode(EntitlementVerificationMode.INFORMATIONAL)
        },
      )
    }
  }

  override suspend fun hasProEntitlement(): Boolean {
    return runCatching {
      Purchases.sharedInstance.awaitCustomerInfo()
        .entitlements[ENTITLEMENT_PRO_ID]?.isActive == true
    }.getOrDefault(false)
  }

  override fun observeProEntitlement(): Flow<Boolean> {
    return flow {
      while (true) {
        emit(hasProEntitlement())
        delay(500.milliseconds)
      }
    }.distinctUntilChanged()
  }

  private companion object {
    const val ENTITLEMENT_PRO_ID = "pro"
  }
}
