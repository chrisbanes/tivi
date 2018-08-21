/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.ui.widget

import android.content.Context
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.support.annotation.AttrRes
import android.support.annotation.MenuRes
import android.support.annotation.StyleRes
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import app.tivi.R

@BindingMethods(
        BindingMethod(type = PopupMenuButton::class, attribute = "popupMenuClickListener", method = "setMenuItemClickListener"),
        BindingMethod(type = PopupMenuButton::class, attribute = "popupMenuListener", method = "setPopupMenuListener")
)
class PopupMenuButton : AppCompatImageButton {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.actionOverflowButtonStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PopupMenuButton)
        val menuRes = a.getResourceId(R.styleable.PopupMenuButton_menu, -1)
        if (menuRes != -1) {
            inflateMenu(menuRes)
        }
        a.recycle()
    }

    val popupMenu: PopupMenu = InternalPopupMenu(context, this, R.attr.actionOverflowMenuStyle)
    var popupMenuListener: PopupMenuListener? = null

    fun inflateMenu(@MenuRes menuRes: Int) = popupMenu.inflate(menuRes)

    fun setMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (popupMenu.dragToOpenListener.onTouch(this, event)) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun performClick(): Boolean {
        popupMenu.show()
        return true
    }

    interface PopupMenuListener {
        fun onPreparePopupMenu(popupMenu: PopupMenu)
    }

    private fun onPrePopupShow() {
        popupMenuListener?.also {
            it.onPreparePopupMenu(popupMenu)
        }
    }

    private inner class InternalPopupMenu(
        context: Context,
        anchor: View,
        gravity: Int = Gravity.NO_GRAVITY,
        @AttrRes popupStyleAttr: Int = R.attr.popupMenuStyle,
        @StyleRes popupStyleRes: Int = 0
    ) : PopupMenu(context, anchor, gravity, popupStyleAttr, popupStyleRes) {
        override fun show() {
            onPrePopupShow()
            super.show()
        }
    }
}