/*
 * Copyright 2020 Google LLC
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

package app.tivi.domain.observers

import app.tivi.data.entities.ShowImages
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.domain.SubjectInteractor
import app.tivi.util.AppCoroutineDispatchers
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveShowImages @Inject constructor(
    private val store: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers
) : SubjectInteractor<ObserveShowImages.Params, ShowImages>() {

    override fun createObservable(params: Params): Flow<ShowImages> {
        return store.stream(StoreRequest.cached(params.showId, refresh = false))
            .map { ShowImages(it.requireData()) }
            .flowOn(dispatchers.computation)
    }

    data class Params(val showId: Long)
}
