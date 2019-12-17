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

package app.tivi.ui

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState

class AuthStateMenuItemBinder(
    private val userMenuItem: MenuItem,
    private val loginMenuItem: MenuItem,
    private val loadImageFunc: (MenuItem, url: String) -> Unit
) {
    fun bind(authState: TraktAuthState, user: TraktUser? = null) {
        if (authState == TraktAuthState.LOGGED_IN) {
            userMenuItem.isVisible = true
            user?.avatarUrl?.also { url -> loadImageFunc(userMenuItem, url) }
            loginMenuItem.isVisible = false
        } else if (authState == TraktAuthState.LOGGED_OUT) {
            userMenuItem.isVisible = false
            loginMenuItem.isVisible = true
        }
    }
}

fun authStateToolbarMenuBinder(
    toolbar: Toolbar,
    userMenuItemId: Int,
    loginMenuItemId: Int,
    loadImageFunc: (MenuItem, url: String) -> Unit
) = AuthStateMenuItemBinder(
    toolbar.menu.findItem(userMenuItemId),
    toolbar.menu.findItem(loginMenuItemId),
    loadImageFunc
)
