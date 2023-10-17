// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.EnTiviStrings
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class StringFormattingTest {
  private val strings = EnTiviStrings

  @Test
  fun rating() {
    assertThat(strings.traktRatingText(76.74f)).isEqualTo("77%")
    assertThat(strings.traktRatingText(40.00f)).isEqualTo("40%")
  }
}
