// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall as RevenueCatPaywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@OptIn(ExperimentalMaterial3Api::class)
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

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  options?.let {
    ModalBottomSheet(
      sheetState = sheetState,
      onDismissRequest = onDismissRequest,
      dragHandle = null,
      windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
      modifier = modifier,
    ) {
      Box(
        modifier = Modifier
          .windowInsetsPadding(BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom)),
      ) {
        RevenueCatPaywall(it)
      }
    }
  }
}
