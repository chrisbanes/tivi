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
 *
 */

package me.banes.chris.tivi.home.library

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_library.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.home.HomeFragment

class LibraryFragment : HomeFragment<LibraryViewModel>() {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(LibraryViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        library_toolbar?.apply {
            title = getString(R.string.home_nav_library)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
    }

    override fun findUserAvatarMenuItem(): MenuItem? {
        return library_toolbar.menu.findItem(R.id.home_menu_user_avatar)
    }

    override fun findUserLoginMenuItem(): MenuItem? {
        return library_toolbar.menu.findItem(R.id.home_menu_user_login)
    }

}
