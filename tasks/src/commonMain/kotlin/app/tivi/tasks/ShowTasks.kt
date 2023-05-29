// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

interface ShowTasks {
    fun syncLibraryShows(deferUntilIdle: Boolean = false)
    fun setupNightSyncs()
}
