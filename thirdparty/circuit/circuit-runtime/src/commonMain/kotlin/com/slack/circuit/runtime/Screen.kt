// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime

import androidx.compose.runtime.Immutable

/**
 * Represents an individual screen, used as a key for `Presenter.Factory` and `Ui.Factory`.
 *
 * Screens can be simple sentinel `object` types or data classes with information to share. Screens
 * with information should contain the minimum amount of data needed for the target presenter to
 * begin presenting state.
 *
 * ```
 * data class AddFavorites(
 *   val externalId: UUID,
 * ) : Screen
 * ```
 *
 * Screens are then passed into a `Navigator` to navigate to them.
 *
 * ```
 * fun showAddFavorites() {
 *  navigator.goTo(
 *    AddFavorites(
 *      externalId = uuidGenerator.generate()
 *    )
 *  )
 * }
 * ```
 */
@Immutable public expect interface Screen
