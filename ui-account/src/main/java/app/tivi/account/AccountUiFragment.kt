/*
 * Copyright 2020 Google LLC
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

package app.tivi.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.extensions.navigateToNavDestination
import app.tivi.settings.TiviPreferences
import app.tivi.trakt.LoginTrakt
import app.tivi.trakt.TraktAuthManager
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
import javax.inject.Inject

@AndroidEntryPoint
class AccountUiFragment : DialogFragment() {
    private val viewModel: AccountUiViewModel by viewModels()

    @Inject internal lateinit var traktAuthManager: TraktAuthManager
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject lateinit var preferences: TiviPreferences

    // Need to lateinit this, since we use a @Inject class to generate the intent
    private lateinit var loginLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginLauncher = registerForActivityResult(
            traktAuthManager.buildActivityResult()
        ) { result: LoginTrakt.Result? ->
            if (result != null) {
                traktAuthManager.onAuthResponse(result)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Workaround for https://issuetracker.google.com/180691023
        dialog?.window?.decorView?.let { decorView ->
            ViewTreeLifecycleOwner.set(decorView, viewLifecycleOwner)
        }

        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

            // We use ViewWindowInsetObserver rather than ProvideWindowInsets
            // See: https://github.com/chrisbanes/accompanist/issues/155
            val windowInsets = ViewWindowInsetObserver(this).start(consumeWindowInsets = false)

            setContent {
                CompositionLocalProvider(
                    LocalTiviDateFormatter provides tiviDateFormatter,
                    LocalWindowInsets provides windowInsets,
                ) {
                    TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                        val viewState by viewModel.liveData.observeAsState()
                        if (viewState != null) {
                            AccountUi(viewState!!, ::onAccountUiAction)
                        }
                    }
                }
            }
        }
    }

    private fun onAccountUiAction(action: AccountUiAction) {
        when (action) {
            is Close -> view?.post(::dismiss)
            is OpenSettings -> {
                view?.post {
                    findNavController().navigateToNavDestination(R.id.navigation_settings)
                }
            }
            is Login -> loginLauncher.launch(Unit)
            is Logout -> viewModel.logout()
        }
    }
}
