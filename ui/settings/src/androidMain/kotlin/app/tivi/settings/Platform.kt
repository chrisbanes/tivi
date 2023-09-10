// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = 31)
internal actual val DynamicColorsAvailable: Boolean = Build.VERSION.SDK_INT >= 31
internal actual val OpenSourceLicenseAvailable: Boolean = true
