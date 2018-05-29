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
import javax.inject.Inject
import javax.inject.Provider

/**
 * JobCreator which uses Dagger to create the instances.
 */
class TiviJobCreator @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<String, Provider<Job>>
) : JobCreator {
    override fun create(tag: String): Job {
        val creator = creators[tag] ?: throw IllegalArgumentException("Unknown job tag: $tag")
        try {
            return creator.get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
