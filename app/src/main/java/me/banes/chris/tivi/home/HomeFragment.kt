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

package me.banes.chris.tivi.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviFragment
import me.banes.chris.tivi.extensions.loadIconFromUrl
import javax.inject.Inject

abstract class HomeFragment<VM : HomeFragmentViewModel> : TiviFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    internal lateinit var viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authUiState.observe(this, Observer {
            when (it) {
                HomeFragmentViewModel.AuthUiState.LOGGED_IN -> {
                    findUserAvatarMenuItem()?.isVisible = true
                    findUserLoginMenuItem()?.isVisible = false
                }
                HomeFragmentViewModel.AuthUiState.LOGGED_OUT -> {
                    findUserAvatarMenuItem()?.isVisible = false
                    findUserLoginMenuItem()?.isVisible = true
                }
            }
        })

        viewModel.userProfileLiveData.observe(this, Observer { user ->
            if (user != null) {
                findUserAvatarMenuItem()?.let {
                    it.title = user.name
                    if (user.avatarUrl != null) {
                        it.loadIconFromUrl(context!!, user.avatarUrl)
                    }
                }
            } else {
                // TODO clear user profile
            }
        })
    }

    open fun onMenuItemClicked(item: MenuItem) = when (item.itemId) {
        R.id.home_menu_user_avatar -> {
            viewModel.onProfileItemClicked()
            true
        }
        R.id.home_menu_user_login -> {
            viewModel.onLoginItemClicked()
            true
        }
        R.id.home_menu_settings -> {
            viewModel.onSettingsItemClicked()
            true
        }
        else -> false
    }

    open fun findUserAvatarMenuItem(): MenuItem? {
        return getMenu()?.findItem(R.id.home_menu_user_avatar)
    }

    open fun findUserLoginMenuItem(): MenuItem? {
        return getMenu()?.findItem(R.id.home_menu_user_login)
    }

    abstract fun getMenu(): Menu?

}
