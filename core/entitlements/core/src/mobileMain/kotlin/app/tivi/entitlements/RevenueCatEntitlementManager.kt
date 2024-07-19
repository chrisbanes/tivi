// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import com.revenuecat.purchases.kmp.EntitlementVerificationMode
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.entitlements
import com.revenuecat.purchases.kmp.get
import com.revenuecat.purchases.kmp.isActive
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import me.tatarka.inject.annotations.Inject

@Inject
class RevenueCatEntitlementManager : EntitlementManager {

  override fun setup() {
    Purchases.configure(
      // TODO change the API key for different platforms
      PurchasesConfiguration(apiKey = BuildConfig.TIVI_REVENUECAT_ANDROID_API_KEY) {
        verificationMode(EntitlementVerificationMode.INFORMATIONAL)
      },
    )
  }

  override suspend fun hasProEntitlement(): Boolean {
    return Purchases.sharedInstance.awaitCustomerInfo()
      .entitlements[ENTITLEMENT_PRO_ID]?.isActive == true
  }

  private companion object {
    const val ENTITLEMENT_PRO_ID = "pro"
  }
}
