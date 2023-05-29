// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.app.Activity
import android.view.View

fun interface ContentViewSetter {
    fun setContentView(activity: Activity, view: View)
}
