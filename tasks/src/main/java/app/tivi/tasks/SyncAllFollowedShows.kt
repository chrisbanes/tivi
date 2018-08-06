/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.tasks

import androidx.work.Worker
import app.tivi.interactors.SyncFollowedShows
import app.tivi.interactors.launchInteractor
import kotlinx.coroutines.experimental.runBlocking
import javax.inject.Inject

class SyncAllFollowedShows @Inject constructor(
    private val syncFollowedShows: SyncFollowedShows
) : Worker() {
    companion object {
        const val TAG = "sync-all-followed-shows"
        const val NIGHTLY_SYNC_TAG = "night-sync-all-followed-shows"
    }

    override fun doWork(): Result {
        AndroidWorkerInjector.inject(this)

        runBlocking {
            launchInteractor(syncFollowedShows, SyncFollowedShows.Params(true)).join()
        }
        return Result.SUCCESS
    }
}