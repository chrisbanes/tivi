// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

typealias TiviUiViewController = (
    onRootPop: () -> Unit,
    onOpenSettings: () -> Unit,
) -> UIViewController

@Inject
fun TiviUiViewController(
    @Assisted onRootPop: () -> Unit,
    @Assisted onOpenSettings: () -> Unit,
    tiviContent: TiviContent,
): UIViewController = ComposeUIViewController {
    tiviContent(
        onRootPop = onRootPop,
        onOpenSettings = onOpenSettings,
        modifier = Modifier,
    )
}
