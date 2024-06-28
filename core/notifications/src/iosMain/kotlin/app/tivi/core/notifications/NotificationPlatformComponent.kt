// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface NotificationPlatformComponent {

  @Provides
  @ApplicationScope
  fun provideNotificationManager(impl: IosNotificationManager): NotificationManager = impl
}
