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
import app.tivi.home.HomeTextCreator
import app.tivi.searchItemShow
import app.tivi.ui.epoxy.EpoxyModelProperty
import com.airbnb.epoxy.EpoxyController
import javax.inject.Inject

class SearchEpoxyController @Inject constructor(
    private val textCreator: HomeTextCreator
) : EpoxyController() {
    var callbacks by EpoxyModelProperty<Callbacks?> { null }
    var viewState by EpoxyModelProperty { SearchViewState() }

    interface Callbacks {
        fun onSearchItemClicked(show: TiviShow)
    }

    override fun buildModels() {
        val searchResult = viewState.searchResults

        searchResult?.results?.forEach { show ->
            searchItemShow {
                id(show.id)
                tiviShow(show)
                textCreator(textCreator)
                tmdbImageUrlProvider(viewState.tmdbImageUrlProvider)
                clickListener { _ ->
                    callbacks?.onSearchItemClicked(show)
                }
            }
        }
    }
}