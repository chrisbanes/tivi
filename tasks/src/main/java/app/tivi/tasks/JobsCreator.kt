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

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module(includes = [JobsModule::class])
abstract class JobsCreator {
    @Binds
    internal abstract fun bindJobCreator(creator: TiviJobCreator): JobCreator

    @Binds
    @IntoMap
    @StringKey(SyncAllFollowedShows.TAG)
    abstract fun bindSyncAllFollowedShows(job: SyncAllFollowedShows): Job

    @Binds
    @IntoMap
    @StringKey(SyncShowWatchedProgress.TAG)
    abstract fun bindSyncShowWatchedProgress(job: SyncShowWatchedProgress): Job
}