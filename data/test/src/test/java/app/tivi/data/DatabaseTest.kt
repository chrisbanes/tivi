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

package app.tivi.data

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.shows.ShowsBinds
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ApplicationScope
import app.tivi.tmdb.TmdbModule
import app.tivi.trakt.TraktModule
import app.tivi.util.AnalyticsModule
import app.tivi.util.LoggerModule
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class DatabaseTest {
    @get:Rule(order = 1)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val component by unsafeLazy {
        TestApplicationComponent::class
    }
}

@Component
@ApplicationScope
abstract class TestApplicationComponent(
    @get:Provides val application: Application,
) : TmdbModule,
    TraktModule,
    AnalyticsModule,
    EpisodeBinds,
    FollowedShowsBinds,
    ShowImagesBinds,
    ShowsBinds,
    LoggerModule,
    TestDataSourceModule(),
    TestDatabaseModule,
    TestRoomDatabaseModule
