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

package me.banes.chris.tivi

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity

/**
 * Base Activity class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviActivity : DaggerAppCompatActivity(), LifecycleRegistryOwner {

    private var lifecycleRegistry: LifecycleRegistry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry!!
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry = null
    }

}
