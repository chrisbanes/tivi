// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.developer.log.DevLogComponent
import app.tivi.developer.notifications.DevNotificationsComponent
import app.tivi.settings.developer.DevSettingsComponent

interface QaUiComponent :
  SharedUiComponent,
  DevLogComponent,
  DevSettingsComponent,
  DevNotificationsComponent
