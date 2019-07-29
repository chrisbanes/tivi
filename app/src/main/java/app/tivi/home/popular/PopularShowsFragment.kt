/*
 * Copyright 2017 Google LLC
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

package app.tivi.home.popular

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.common.layouts.PosterGridItemBindingModel_
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.util.EntryGridEpoxyController
import app.tivi.util.EntryGridFragment
import com.airbnb.epoxy.EpoxyModel

class PopularShowsFragment : EntryGridFragment<PopularEntryWithShow, PopularShowsViewModel>(PopularShowsViewModel::class.java) {
    internal fun onItemClicked(item: PopularEntryWithShow) {
        val sharedElements = SharedElementHelper()
        binding.gridRecyclerview.findViewHolderForItemId(item.generateStableId()).let {
            sharedElements.addSharedElement(it.itemView, "poster")
        }

        findNavController().navigate(
                app.tivi.home.followed.R.id.activity_show_details,
                bundleOf("show_id" to item.show.id),
                null,
                sharedElements.toActivityNavigatorExtras(requireActivity())
        )
    }

    override fun createController(): EntryGridEpoxyController<PopularEntryWithShow> {
        return object : EntryGridEpoxyController<PopularEntryWithShow>(R.string.discover_popular) {
            override fun buildItemModel(item: PopularEntryWithShow): EpoxyModel<*> {
                return PosterGridItemBindingModel_()
                        .id(item.generateStableId())
                        .tmdbImageUrlProvider(tmdbImageUrlProvider)
                        .posterImage(item.images.findHighestRatedPoster())
                        .tiviShow(item.show)
                        .transitionName(item.show.homepage)
                        .clickListener(View.OnClickListener { onItemClicked(item) })
            }
        }
    }
}
