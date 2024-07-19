// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall as RevenueCatPaywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
fun Paywall(
  onDismissRequest: () -> Unit,
) {
  val lastOnDismissRequest by rememberUpdatedState(onDismissRequest)

  val options = remember {
    PaywallOptions(dismissRequest = lastOnDismissRequest) {
      // todo
    }
  }

  RevenueCatPaywall(options)
}
