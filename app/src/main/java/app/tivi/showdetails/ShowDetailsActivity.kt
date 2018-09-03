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

package app.tivi.showdetails

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.tivi.R
import app.tivi.TiviActivity
import app.tivi.extensions.observeNotNull
import app.tivi.showdetails.details.ShowDetailsFragment
import app.tivi.showdetails.details.ShowDetailsFragmentViewModel
import app.tivi.showdetails.episodedetails.EpisodeDetailsFragment
import app.tivi.showdetails.episodedetails.EpisodeDetailsViewModel
import javax.inject.Inject

class ShowDetailsActivity : TiviActivity() {

    companion object {
        private const val KEY_SHOW_ID = "show_id"

        fun createIntent(context: Context, id: Long): Intent {
            return Intent(context, ShowDetailsActivity::class.java).apply {
                putExtra(KEY_SHOW_ID, id)
            }
        }
    }

    private lateinit var navigatorViewModel: ShowDetailsNavigatorViewModel

    @Inject lateinit var showDetailsFragmentViewModelFactory: ShowDetailsFragmentViewModel.Factory
    @Inject lateinit var episodeDetailsViewModelFactory: EpisodeDetailsViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_details)

        navigatorViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(ShowDetailsNavigatorViewModel::class.java)

        navigatorViewModel.events.observeNotNull(this) {
            when (it) {
                is NavigateUpEvent -> onNavigateUp()
                is ShowEpisodeDetailsEvent -> showEpisodeDetails(it.episodeId)
            }
        }

        postponeEnterTransition()
    }

    override fun handleIntent(intent: Intent) {
        val showId = intent.getLongExtra(KEY_SHOW_ID, -1L)
        if (showId != -1L) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.details_content, ShowDetailsFragment.create(showId))
                    .commit()
        } else {
            // TODO finish?
        }
    }

    private fun showEpisodeDetails(episodeId: Long) {
        EpisodeDetailsFragment.create(episodeId)
                .show(supportFragmentManager, "episode")
    }
}