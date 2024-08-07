// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

internal object CrashlyticsIosLoggerWriter : LogWriter() {

//  override fun setUserId(id: String) {
//    CrashlyticsKotlin.setCustomValue("username", id)
//  }

  override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
    CrashlyticsKotlin.logMessage(message)
    if (throwable != null) {
      CrashlyticsKotlin.sendHandledException(throwable)
    }
  }
}
