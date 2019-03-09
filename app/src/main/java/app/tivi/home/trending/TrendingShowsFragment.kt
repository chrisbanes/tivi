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

package app.tivi.home.trending

import androidx.navigation.fragment.findNavController
import app.tivi.PosterGridItemBindingModel_
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.util.EntryGridEpoxyController
import app.tivi.util.EntryGridFragment
import kotlinx.android.synthetic.main.fragment_rv_grid.*

class TrendingShowsFragment : EntryGridFragment<TrendingEntryWithShow, TrendingShowsViewModel>(TrendingShowsViewModel::class.java) {
    override fun createController(): EntryGridEpoxyController<TrendingEntryWithShow> {
        return object : EntryGridEpoxyController<TrendingEntryWithShow>() {
            override fun buildItemModel(item: TrendingEntryWithShow): PosterGridItemBindingModel_ {
                return super.buildItemModel(item)
                        .annotationLabel(item.entry?.watchers.toString())
                        .annotationIcon(R.drawable.ic_eye_12dp)
            }
        }
    }

    override fun onItemClicked(item: TrendingEntryWithShow) {
        val sharedElements = SharedElementHelper()
        grid_recyclerview.findViewHolderForItemId(item.generateStableId())?.let {
            sharedElements.addSharedElement(it.itemView, "poster")
        }

        val direction = TrendingShowsFragmentDirections.actionTrendingToActivityShowDetails(item.show.id)
        findNavController().navigate(direction, sharedElements.toActivityNavigatorExtras(requireActivity()))
    }
}
