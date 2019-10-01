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

package app.tivi.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.commitNow
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import app.tivi.R
import app.tivi.databinding.FragmentSettingsBinding
import app.tivi.extensions.doOnApplyWindowInsets
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {
    private lateinit var binding: FragmentSettingsBinding

    @Inject lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsToolbar.setupWithNavController(findNavController(), appBarConfiguration)

        binding.statusScrim.doOnApplyWindowInsets { scrim, insets, _, _ ->
            val lp = scrim.layoutParams as ConstraintLayout.LayoutParams
            if (lp.height != insets.systemWindowInsetTop) {
                lp.height = insets.systemWindowInsetTop
                lp.validate()
                scrim.requestLayout()
            }
        }

        childFragmentManager.commitNow {
            replace(R.id.settings_container, SettingsPreferenceFragment())
        }
    }
}
