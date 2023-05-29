// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import me.tatarka.inject.annotations.Provides

interface PowerControllerComponent {
    @Provides
    fun providePowerController(bind: AndroidPowerController): PowerController = bind
}
