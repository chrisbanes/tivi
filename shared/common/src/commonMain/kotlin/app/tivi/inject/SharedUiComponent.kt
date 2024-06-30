// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.account.AccountComponent
import app.tivi.common.ui.resources.Locales
import app.tivi.common.ui.resources.Strings
import app.tivi.common.ui.resources.TiviStrings
import app.tivi.core.permissions.PermissionsController
import app.tivi.episode.track.EpisodeTrackComponent
import app.tivi.episodedetails.EpisodeDetailsComponent
import app.tivi.home.RootUiComponent
import app.tivi.home.TiviContent
import app.tivi.home.discover.DiscoverComponent
import app.tivi.home.library.LibraryComponent
import app.tivi.home.popular.PopularShowsComponent
import app.tivi.home.recommended.RecommendedShowsComponent
import app.tivi.home.search.SearchComponent
import app.tivi.home.trending.TrendingShowsComponent
import app.tivi.home.upnext.UpNextComponent
import app.tivi.settings.SettingsComponent
import app.tivi.settings.licenses.LicensesComponent
import app.tivi.showdetails.details.ShowDetailsComponent
import app.tivi.showdetails.seasons.ShowSeasonsComponent
import cafe.adriel.lyricist.Lyricist
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.Provides

interface SharedUiComponent :
  AccountComponent,
  DiscoverComponent,
  EpisodeDetailsComponent,
  EpisodeTrackComponent,
  LibraryComponent,
  PopularShowsComponent,
  RecommendedShowsComponent,
  SearchComponent,
  SettingsComponent,
  LicensesComponent,
  ShowDetailsComponent,
  ShowSeasonsComponent,
  RootUiComponent,
  TrendingShowsComponent,
  UpNextComponent {

  val tiviContent: TiviContent
  val permissionsController: PermissionsController

  @Provides
  @ActivityScope
  fun provideLyricist(): TiviStrings = Lyricist(
    defaultLanguageTag = Locales.EN,
    translations = Strings,
  ).strings

  @Provides
  @ActivityScope
  fun provideCircuit(
    uiFactories: Set<Ui.Factory>,
    presenterFactories: Set<Presenter.Factory>,
  ): Circuit = Circuit.Builder()
    .addUiFactories(uiFactories)
    .addPresenterFactories(presenterFactories)
    .build()
}
