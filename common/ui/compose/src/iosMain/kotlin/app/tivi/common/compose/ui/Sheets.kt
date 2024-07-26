// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.interop.LocalUIViewController
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject

@Composable
fun PresentSheetViewController(
  viewController: UIViewController,
  onDismissRequest: () -> Unit,
  detents: List<UISheetPresentationControllerDetent> = listOf(UISheetPresentationControllerDetent.mediumDetent()),
  host: UIViewController = LocalUIViewController.current,
) {
  DisposableEffect(viewController, host) {
    viewController.sheetPresentationController?.apply {
      delegate = SheetDelegate(onDismissRequest)
      this.detents = detents
    }

    host.presentViewController(viewController, true, null)

    onDispose {
      viewController.dismissViewControllerAnimated(true) {
        onDismissRequest()
      }
    }
  }
}

private class SheetDelegate(
  private val onDismiss: () -> Unit,
) : NSObject(),
  UIAdaptivePresentationControllerDelegateProtocol {
  override fun presentationControllerShouldDismiss(presentationController: UIPresentationController): Boolean {
    return true
  }

  override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
    onDismiss()
  }
}
