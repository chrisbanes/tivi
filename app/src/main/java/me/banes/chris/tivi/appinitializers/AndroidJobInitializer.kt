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

package me.banes.chris.tivi.appinitializers

import android.app.Application
import com.evernote.android.job.JobManager
import me.banes.chris.tivi.tasks.TiviJobCreator
import javax.inject.Inject

class AndroidJobInitializer @Inject constructor(
    private val jobManager: JobManager,
    private val jobCreator: TiviJobCreator
) : AppInitializer {
    override fun init(application: Application) {
        jobManager.addJobCreator(jobCreator)
    }
}