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

package app.tivi.showdetails.details.view

import android.view.ViewGroup
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.staticAmbientOf
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.drawBorder
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.ScaleFit
import androidx.ui.layout.Column
import androidx.ui.layout.FlowRow
import androidx.ui.layout.Row
import androidx.ui.layout.SizeMode
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredSize
import androidx.ui.layout.preferredWidth
import androidx.ui.layout.wrapContentHeight
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.res.stringResource
import androidx.ui.unit.dp
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.LoadNetworkImageWithCrossfade
import app.tivi.common.compose.MaterialThemeFromAndroidTheme
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.WrapWithAmbients
import app.tivi.common.compose.observe
import app.tivi.common.compose.observeInsets
import app.tivi.common.compose.paddingHV
import app.tivi.common.compose.setContentWithLifecycle
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.showdetails.details.ShowDetailsAction
import app.tivi.showdetails.details.ShowDetailsViewState
import app.tivi.util.TiviDateFormatter

val ShowDetailsTextCreatorAmbient = staticAmbientOf<ShowDetailsTextCreator>()

fun ViewGroup.composeShowDetails(
    lifecycleOwner: LifecycleOwner,
    state: LiveData<ShowDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (ShowDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter,
    textCreator: ShowDetailsTextCreator
): Any = setContentWithLifecycle(lifecycleOwner) {
    WrapWithAmbients(tiviDateFormatter, InsetsHolder()) {
        Providers(ShowDetailsTextCreatorAmbient provides textCreator) {
            observeInsets(insets)

            val viewState = observe(state)
            if (viewState != null) {
                MaterialThemeFromAndroidTheme(context) {
                    ShowDetails(viewState, actioner)
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ShowDetails(
    viewState: ShowDetailsViewState,
    actioner: (ShowDetailsAction) -> Unit
) {
    VerticalScroller {
        Column {
            val backdropImage = viewState.backdropImage
            Surface(modifier = Modifier.aspectRatio(16f / 10)) {
                Stack {
                    if (backdropImage != null) {
                        LoadNetworkImageWithCrossfade(
                            modifier = Modifier.matchParent(),
                            data = backdropImage,
                            scaleFit = ScaleFit.FillMinDimension
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(Alignment.TopStart),
                elevation = 2.dp
            ) {
                Column {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = viewState.show.title ?: "Show",
                        style = MaterialTheme.typography.h6
                    )

                    Row {
                        Spacer(modifier = Modifier.preferredWidth(16.dp))

                        val poster = viewState.posterImage
                        if (poster != null) {
                            LoadNetworkImageWithCrossfade(
                                modifier = Modifier.weight(1f, fill = false)
                                    .aspectRatio(2 / 3f),
                                data = poster,
                                scaleFit = ScaleFit.FillMinDimension,
                                alignment = Alignment.TopStart
                            )
                        }

                        Spacer(modifier = Modifier.preferredWidth(16.dp))

                        Box(Modifier.weight(1f, fill = false)) {
                            FlowRow(
                                mainAxisSize = SizeMode.Expand,
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                TraktRatingInfoPanel(viewState.show)

                                NetworkInfoPanel(viewState.show)

                                CertificateInfoPanel(viewState.show)

                                RuntimeInfoPanel(viewState.show)

                                AirsInfoPanel(viewState.show)
                            }
                        }

                        Spacer(modifier = Modifier.preferredWidth(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier.None
) {
    Column(modifier) {
        Text(
            text = stringResource(id = R.string.network_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        val networkLogo = show.networkLogoPath
        if (!networkLogo.isNullOrEmpty()) {
            LoadNetworkImageWithCrossfade(
                modifier = Modifier.preferredSize(72.dp, 32.dp),
                data = ShowTmdbImage(path = networkLogo, type = ImageType.LOGO, showId = 0)
            )
        } else {
            val network = show.network
            if (network != null) {
                Text(
                    text = network,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
private fun RuntimeInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier.None
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.runtime_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        Text(
            text = stringResource(R.string.minutes_format, show.runtime ?: 0),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun AirsInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier.None
) {
    val textCreator = ShowDetailsTextCreatorAmbient.current
    val text = textCreator.airsText(show)?.toString()

    if (!text.isNullOrEmpty()) {
        Column(modifier) {
            Text(
                text = stringResource(R.string.airs_title),
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(Modifier.preferredHeight(4.dp))

            Text(text = text, style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
private fun CertificateInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier.None
) {
    val cert = show.certification
    if (!cert.isNullOrEmpty()) {
        Column(modifier) {
            Text(
                text = stringResource(R.string.certificate_title),
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(Modifier.preferredHeight(4.dp))

            Text(
                text = cert,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.drawBorder(
                    size = 1.dp,
                    color = MaterialTheme.colors.onSurface,
                    shape = RoundedCornerShape(2.dp)
                ).paddingHV(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun TraktRatingInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier.None
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.trakt_rating_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        Row {
            VectorImage(
                modifier = Modifier.preferredSize(32.dp),
                id = R.drawable.ic_star_black_24dp,
                tintColor = MaterialTheme.colors.secondaryVariant,
                scaleFit = ScaleFit.FillMinDimension
            )

            Spacer(Modifier.preferredWidth(4.dp))

            Column {
                Text(
                    text = stringResource(R.string.trakt_rating_text,
                        (show.traktRating ?: 0f) * 10f),
                    style = MaterialTheme.typography.body2
                )

                Text(
                    text = stringResource(R.string.trakt_rating_votes,
                        (show.traktVotes ?: 0) / 1000f),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}
