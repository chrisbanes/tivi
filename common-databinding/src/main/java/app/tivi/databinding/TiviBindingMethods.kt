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

package app.tivi.databinding

import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.tivi.ui.widget.PopupMenuButton

@BindingMethods(
    BindingMethod(type = View::class, attribute = "outlineProviderInstance", method = "setOutlineProvider"),
    BindingMethod(type = SwipeRefreshLayout::class, attribute = "isRefreshing", method = "setRefreshing"),
    BindingMethod(type = View::class, attribute = "clipToOutline", method = "setClipToOutline"),
    BindingMethod(type = View::class, attribute = "activated", method = "setActivated"),
    BindingMethod(type = View::class, attribute = "selected", method = "setSelected"),
    BindingMethod(type = View::class, attribute = "onLongClick", method = "setOnLongClickListener"),
    BindingMethod(type = PopupMenuButton::class, attribute = "popupMenuClickListener", method = "setMenuItemClickListener"),
    BindingMethod(type = PopupMenuButton::class, attribute = "popupMenuListener", method = "setPopupMenuListener")
)
class TiviBindingMethods
