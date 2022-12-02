/*
 * Copyright 2019 Google LLC
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

package app.tivi.data.mappers

import app.tivi.data.entities.ShowStatus
import com.uwetrottmann.trakt5.enums.Status
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktStatusToShowStatus @Inject constructor() : Mapper<Status, ShowStatus> {
    override suspend fun map(from: Status) = when (from) {
        Status.ENDED -> ShowStatus.ENDED
        Status.RETURNING -> ShowStatus.RETURNING
        Status.CANCELED -> ShowStatus.CANCELED
        Status.IN_PRODUCTION -> ShowStatus.IN_PRODUCTION
    }
}
