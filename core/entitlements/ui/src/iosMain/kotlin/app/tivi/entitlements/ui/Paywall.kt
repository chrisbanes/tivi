// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.common.compose.ui.PresentSheetViewController
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.get
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall as RevenueCatPaywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import platform.UIKit.UISheetPresentationControllerDetent

@Composable
actual fun Paywall(
  onDismissRequest: () -> Unit,
  modifier: Modifier,
) {
  val lastOnDismissRequest by rememberUpdatedState(onDismissRequest)

  val options by produceState<PaywallOptions?>(initialValue = null) {
    val offerings = Purchases.sharedInstance.awaitOfferings()

    value = PaywallOptions(dismissRequest = lastOnDismissRequest) {
      offering = offerings["pro"]
    }
  }

  val controller = remember {
    ComposeUIViewController {
      options?.let { RevenueCatPaywall(it) }
    }
  }

  PresentSheetViewController(
    viewController = controller,
    onDismissRequest = { onDismissRequest() },
    detents = listOf(UISheetPresentationControllerDetent.largeDetent()),
  )
}
