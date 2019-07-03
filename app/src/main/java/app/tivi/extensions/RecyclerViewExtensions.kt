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

package app.tivi.extensions

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.createAndBind(parent: ViewGroup, position: Int): VH {
    val vh = onCreateViewHolder(parent, getItemViewType(position))
    onBindViewHolder(vh, position)
    return vh
}