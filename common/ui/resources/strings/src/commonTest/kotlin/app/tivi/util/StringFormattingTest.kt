// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.fmt
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StringFormattingTest {
  @Test
  fun rating() {
    assertThat("%.0f".fmt(76.74f)).isEqualTo("77")
    assertThat("%.0f".fmt(40.00f)).isEqualTo("40")
  }
}
