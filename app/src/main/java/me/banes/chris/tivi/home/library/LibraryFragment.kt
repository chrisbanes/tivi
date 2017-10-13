/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.home.library

import kotlinx.android.synthetic.main.fragment_rv_grid.*
import me.banes.chris.tivi.data.entities.WatchedEntry
import me.banes.chris.tivi.data.entities.WatchedListItem
import me.banes.chris.tivi.util.EntryGridFragment

class LibraryFragment : EntryGridFragment<WatchedEntry, WatchedListItem, LibraryViewModel>(LibraryViewModel::class.java) {

    internal fun scrollToTop() {
        grid_recyclerview.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
    }

}
