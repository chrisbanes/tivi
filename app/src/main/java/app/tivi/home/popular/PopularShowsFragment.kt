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

import androidx.navigation.fragment.findNavController
import app.tivi.SharedElementHelper
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.util.EntryGridFragment

class PopularShowsFragment : EntryGridFragment<PopularEntryWithShow, PopularShowsViewModel>(PopularShowsViewModel::class.java) {
    override fun onItemClicked(item: PopularEntryWithShow) {
        val sharedElements = SharedElementHelper()
        binding.gridRecyclerview.findViewHolderForItemId(item.generateStableId()).let {
            sharedElements.addSharedElement(it.itemView, "poster")
        }

        val direction = PopularShowsFragmentDirections.actionPopularToActivityShowDetails(item.show.id)
        findNavController().navigate(direction, sharedElements.toActivityNavigatorExtras(requireActivity()))
    }
}
