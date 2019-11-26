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

package app.tivi.home.search

import app.tivi.data.entities.TiviShow
import app.tivi.extensions.observable
import app.tivi.home.HomeTextCreator
import com.airbnb.epoxy.EpoxyController
import dagger.Lazy
import javax.inject.Inject

internal class SearchEpoxyController @Inject constructor(
    private val textCreator: Lazy<HomeTextCreator>
) : EpoxyController() {
    var callbacks: Callbacks? by observable(null, ::requestModelBuild)
    var state by observable(SearchViewState(), ::requestModelBuild)

    interface Callbacks {
        fun onSearchItemClicked(show: TiviShow)
    }

    override fun buildModels() {
        val searchResult = state.searchResults

        searchResult?.results?.forEach { showDetailed ->
            searchItemShow {
                id(showDetailed.show.id)
                tiviShow(showDetailed.show)
                posterImage(showDetailed.poster)
                textCreator(textCreator.get())
                clickListener { _ -> callbacks?.onSearchItemClicked(showDetailed.show) }
            }
        }
    }

    fun clear() {
        callbacks = null
    }
}
