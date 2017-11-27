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

package me.banes.chris.tivi.details

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import me.banes.chris.tivi.R

class DetailPosterBehavior(context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<ImageView>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ImageView, dependency: View): Boolean {
        return dependency is RecyclerView
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: ImageView, layoutDirection: Int): Boolean {
        // Let the parent layout the child first
        parent.onLayoutChild(child, layoutDirection)

        val posterLp = child.layoutParams as CoordinatorLayout.LayoutParams
        val rv = parent.findViewById<RecyclerView>(R.id.details_rv)

        val firstChildItemRv = rv.findViewHolderForAdapterPosition(0)
        if (firstChildItemRv != null) {
            // This is the title item
            val childView = firstChildItemRv.itemView
            ViewCompat.offsetLeftAndRight(child, posterLp.marginStart + (childView.left + rv.left) - child.left)
            ViewCompat.offsetTopAndBottom(child, childView.bottom + rv.top - childView.paddingBottom - child.bottom)
        }
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, poster: ImageView, dependency: View): Boolean {
        val rv = dependency as RecyclerView
        val posterLp = poster.layoutParams as CoordinatorLayout.LayoutParams

        val firstChildItemRv = rv.findViewHolderForAdapterPosition(0)
        if (firstChildItemRv != null) {
            // This is the title item
            val childView = firstChildItemRv.itemView
            poster.translationX = (posterLp.marginStart + (childView.left + rv.left) - poster.left).toFloat()
            poster.translationY = ((childView.bottom + rv.top - childView.paddingBottom) - poster.bottom).toFloat()
            return true
        }

        return false
    }

}