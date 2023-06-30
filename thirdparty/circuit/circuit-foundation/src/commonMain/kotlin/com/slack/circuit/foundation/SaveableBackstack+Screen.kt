// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.runtime.Screen

public fun SaveableBackStack.push(screen: Screen) {
  push(
    SaveableBackStack.Record(
      route = checkNotNull(screen::class.simpleName),
      args = mapOf("screen" to screen),
      key = screen.hashCode().toString()
    )
  )
}

public val SaveableBackStack.Record.screen: Screen
  get() = args.getValue("screen") as Screen
