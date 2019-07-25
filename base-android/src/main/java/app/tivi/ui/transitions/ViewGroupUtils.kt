/*
 * Copyright 2018 Google LLC
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

package app.tivi.ui.transitions

import android.view.ViewGroup
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private var sSuppressLayoutMethod: Method? = null
private var sSuppressLayoutMethodFetched: Boolean = false

fun ViewGroup.suppressLayoutInternal(suppress: Boolean) {
    if (!sSuppressLayoutMethodFetched) {
        try {
            sSuppressLayoutMethod = ViewGroup::class.java.getDeclaredMethod("suppressLayout",
                    Boolean::class.javaPrimitiveType)
            sSuppressLayoutMethod!!.isAccessible = true
        } catch (e: NoSuchMethodException) {
            Timber.i(e, "Failed to retrieve suppressLayout method")
        }
        sSuppressLayoutMethodFetched = true
    }
    if (sSuppressLayoutMethod != null) {
        try {
            sSuppressLayoutMethod!!.invoke(this, suppress)
        } catch (e: IllegalAccessException) {
            Timber.i(e, "Failed to invoke suppressLayout method")
        } catch (e: InvocationTargetException) {
            Timber.i(e, "Error invoking suppressLayout method")
        }
    }
}
