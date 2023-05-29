// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import app.tivi.ContentViewSetter
import me.tatarka.inject.annotations.Provides

interface ContentViewSetterComponent {
    @Provides
    fun provideContentViewSetter(): ContentViewSetter = ContentViewSetter { activity, view ->
        activity.setContentView(view)
    }
}
