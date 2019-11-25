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

package app.tivi.common.epoxy

import com.airbnb.epoxy.EpoxyController
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class EpoxyModelProperty<T>(default: () -> T) : ReadWriteProperty<EpoxyController, T> {
    private var defaultProvider: (() -> T)? = default

    private object UNASSIGNED

    // backing field so we can distinguish between null and not yet set.
    // This lets us defer calculating the default value
    private var _value: Any? = UNASSIGNED
        set(value) {
            field = value
            if (value != UNASSIGNED) {
                // Clear the reference to the provider so that we don't hold on to it once a value is set
                defaultProvider = null
            }
        }

    private var value: T
        set(value) {
            _value = value
        }
        get() {
            if (_value == UNASSIGNED) {
                _value = defaultProvider?.invoke()
            }

            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun getValue(thisRef: EpoxyController, property: KProperty<*>): T = value

    override fun setValue(thisRef: EpoxyController, property: KProperty<*>, value: T) {
        if (this.value != value) {
            this.value = value
            thisRef.requestModelBuild()
        }
    }
}
