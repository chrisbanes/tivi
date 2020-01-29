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

package app.tivi.episodedetails

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.Composition
import androidx.compose.ambient
import androidx.compose.state
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LiveData
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.OnChildPositioned
import androidx.ui.core.Text
import androidx.ui.core.WithDensity
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.background
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.withSave
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.EdgeInsets
import androidx.ui.layout.LayoutAlign
import androidx.ui.layout.LayoutAspectRatio
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.material.EmphasisLevels
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Density
import androidx.ui.unit.Px
import androidx.ui.unit.PxPosition
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
import androidx.ui.unit.toOffset
import androidx.ui.unit.toRect
import app.tivi.animation.invoke
import app.tivi.common.compose.GradientScrim
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.LayoutPadding
import app.tivi.common.compose.LoadAndShowImage
import app.tivi.common.compose.MaterialThemeFromAndroidTheme
import app.tivi.common.compose.SwipeDirection
import app.tivi.common.compose.SwipeToDismiss
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.WrapInAmbients
import app.tivi.common.compose.center
import app.tivi.common.compose.observe
import app.tivi.common.compose.observeInsets
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.episodedetails.compose.R
import app.tivi.ui.animations.lerp
import app.tivi.util.TiviDateFormatter
import kotlin.math.hypot

/**
 * This is a bit of hack. I can't make `ui-episodedetails` depend on any of the compose libraries,
 * so I wrap `setContext` as my own function, which `ui-episodedetails` can use.
 *
 * We need to return an `Any` since this method will be called from modules which do not depend
 * on Compose
 */
fun ViewGroup.composeEpisodeDetails(
    state: LiveData<EpisodeDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (EpisodeDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = setContent {
    WrapInAmbients(tiviDateFormatter, InsetsHolder()) {
        observeInsets(insets)

        val viewState = observe(state)
        if (viewState != null) {
            MaterialThemeFromAndroidTheme(context) {
                EpisodeDetails(viewState, actioner)
            }
        }
    }
}

/**
 * We need to return an `Any` since this method will be called from modules which do not depend
 * on Compose
 */
fun disposeComposition(composition: Any) {
    (composition as? Composition)?.dispose()
}

@Composable
private fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Stack {
        Column {
            viewState.episode?.let {
                Backdrop(episode = it)
            }
            VerticalScroller(modifier = LayoutFlexible(1f)) {
                Column {
                    val episode = viewState.episode
                    if (episode != null) {
                        InfoPanes(episode)
                        Summary(episode)
                    }

                    val watches = viewState.watches
                    if (watches.isNotEmpty()) {
                        Header()
                    }
                    watches.forEach { watch ->
                        SwipeToDismiss(
                            // TODO: this should change to START eventually
                            swipeDirections = listOf(SwipeDirection.LEFT),
                            onSwipeComplete = {
                                actioner(RemoveEpisodeWatchAction(watch.id))
                            },
                            swipeChildren = { swipeProgress, _ ->
                                EpisodeWatch(
                                    episodeWatchEntry = watch,
                                    drawBackground = swipeProgress != 0f
                                )
                            },
                            backgroundChildren = { swipeProgress, completeOnRelease ->
                                EpisodeWatchSwipeBackground(swipeProgress, completeOnRelease)
                            }
                        )
                    }
                }
            }
        }

        val insets = ambient(InsetsAmbient)
        WithDensity {
            WatchButton(
                modifier = LayoutGravity.BottomRight +
                    LayoutPadding(horizontal = 16.dp, bottom = 16.dp + insets.bottom.toDp()),
                action = viewState.action,
                actioner = actioner
            )
        }
    }
}

@Composable
private fun Backdrop(episode: Episode) {
    Surface(
        color = MaterialTheme.colors().onSurface.copy(alpha = 0.1f),
        modifier = LayoutAspectRatio(16f / 9)
    ) {
        Stack {
            if (episode.tmdbBackdropPath != null) {
                LoadAndShowImage(modifier = LayoutGravity.Stretch, data = episode)
            }

            Container(modifier = LayoutGravity.Stretch) {
                GradientScrim(baseColor = Color.Black)
            }

            val type = MaterialTheme.typography()
            Text(
                text = episode.title ?: "No title",
                style = type.h6.copy(color = Color.White),
                modifier = LayoutPadding(all = 16.dp) + LayoutGravity.BottomLeft
            )
        }
    }
}

@Composable
private fun InfoPanes(episode: Episode) {
    Row(
        arrangement = Arrangement.SpaceEvenly
    ) {
        episode.traktRating?.let { rating ->
            InfoPane(
                modifier = LayoutFlexible(1f),
                iconResId = R.drawable.ic_details_rating,
                label = stringResource(R.string.trakt_rating_text, rating * 10f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = ambient(TiviDateFormatterAmbient)
            InfoPane(
                modifier = LayoutFlexible(1f),
                iconResId = R.drawable.ic_details_date,
                label = formatter.formatShortRelativeTime(firstAired)
            )
        }
    }
}

@Composable
private fun InfoPane(
    modifier: Modifier = Modifier.None,
    @DrawableRes iconResId: Int,
    label: String
) {
    Column(
        modifier = modifier + LayoutPadding(all = 16.dp)
    ) {
        ProvideEmphasis(emphasis = EmphasisLevels().medium) {
            VectorImage(
                modifier = LayoutAlign.CenterHorizontally,
                id = iconResId
            )
        }

        Spacer(modifier = LayoutHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisLevels().high) {
            Text(
                modifier = LayoutAlign.CenterHorizontally,
                text = label,
                style = MaterialTheme.typography().body1
            )
        }
    }
}

@Composable
private fun Summary(episode: Episode) {
    Surface {
        Ripple(bounded = false) {
            Padding(padding = EdgeInsets(16.dp)) {
                val expanded = state { false }
                Clickable(onClick = { expanded.value = !expanded.value }) {
                    ProvideEmphasis(emphasis = EmphasisLevels().high) {
                        Text(
                            text = episode.summary ?: "No summary",
                            style = MaterialTheme.typography().body2,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded.value) Int.MAX_VALUE else 4
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Padding(padding = EdgeInsets(16.dp, 8.dp, 16.dp, 8.dp)) {
        ProvideEmphasis(emphasis = EmphasisLevels().high) {
            Text(
                text = stringResource(R.string.episode_watches),
                style = MaterialTheme.typography().subtitle1
            )
        }
    }
}

@Composable
private fun EpisodeWatch(
    episodeWatchEntry: EpisodeWatchEntry,
    drawBackground: Boolean = false
) {
    Surface(
        color = if (drawBackground) MaterialTheme.colors().surface else Color.Transparent
    ) {
        Row(modifier = LayoutPadding(16.dp, 8.dp, 16.dp, 8.dp) + LayoutSize.Min(40.dp)) {
            ProvideEmphasis(emphasis = EmphasisLevels().high) {
                val formatter = ambient(TiviDateFormatterAmbient)
                Text(
                    modifier = LayoutFlexible(1f) + LayoutGravity.Center,
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = MaterialTheme.typography().body2
                )
            }

            ProvideEmphasis(emphasis = EmphasisLevels().medium) {
                if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                    VectorImage(
                        id = R.drawable.ic_upload_24dp,
                        modifier = LayoutPadding(left = 8.dp) + LayoutGravity.Center
                    )
                }

                VectorImage(
                    id = when (episodeWatchEntry.pendingAction) {
                        PendingAction.DELETE -> R.drawable.ic_eye_off_24dp
                        else -> R.drawable.ic_eye_24dp
                    },
                    modifier = LayoutPadding(left = 8.dp) + LayoutGravity.Center
                )
            }
        }
    }
}

@Composable
private fun EpisodeWatchSwipeBackground(
    swipeProgress: Float,
    wouldCompleteOnRelease: Boolean = false
) {
    Stack(
        modifier = background(MaterialTheme.colors().onSurface.copy(alpha = 0.2f))
    ) {
        val iconCenter = state { PxPosition(Px.Zero, Px.Zero) }

        val maxRadius = hypot(
            iconCenter.value.x.value.toDouble(),
            iconCenter.value.y.value.toDouble()
        )

        // This container allows us to draw the expanding circle which grows as the user
        // swipes. The circle is drawn via the circleDrawModifier()
        Container(
            modifier = circleDrawModifier(
                // TODO: ideally we'd animate this color state change
                when {
                    wouldCompleteOnRelease -> MaterialTheme.colors().secondary
                    else -> MaterialTheme.colors().onSurface.copy(alpha = 0.3f)
                },
                iconCenter.value.toOffset(),
                // A simple lerp with acceleration
                lerp(0f, maxRadius.toFloat(), fastOutLinearIn(swipeProgress))
            ),
            expanded = true,
            children = {}
        )

        OnChildPositioned(onPositioned = { iconCenter.value = it.center }) {
            ProvideEmphasis(emphasis = EmphasisLevels().medium) {
                VectorImage(
                    id = R.drawable.ic_eye_off_24dp,
                    modifier = LayoutPadding(right = 16.dp) + LayoutGravity.CenterRight
                )
            }
        }
    }
}

private fun circleDrawModifier(
    color: Color,
    centerPoint: Offset,
    radius: Float
) = object : DrawModifier {
    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = color
    }

    override fun draw(density: Density, drawContent: () -> Unit, canvas: Canvas, size: PxSize) {
        canvas.withSave {
            canvas.clipRect(size.toRect())
            canvas.drawCircle(centerPoint, radius, paint)
        }
    }
}

@Composable
fun WatchButton(
    modifier: Modifier = Modifier.None,
    action: Action,
    actioner: (EpisodeDetailsAction) -> Unit
) = FloatingActionButton(
    modifier = modifier,
    color = MaterialTheme.colors().secondary,
    onClick = {
        actioner(
            when (action) {
                Action.WATCH -> AddEpisodeWatchAction
                Action.UNWATCH -> RemoveAllEpisodeWatchesAction
            }
        )
    }
) {
    VectorImage(
        id = when (action) {
            Action.WATCH -> R.drawable.ic_eye_24dp
            Action.UNWATCH -> R.drawable.ic_eye_off_24dp
        }
    )
}

@Preview
@Composable
fun previewEpisodeDetails() = EpisodeDetails(
    viewState = EpisodeDetailsViewState(
        episodeId = 0,
        episode = Episode(
            seasonId = 100,
            title = "A show too far"
        ),
        season = Season(
            id = 100,
            showId = 0
        )
    ),
    actioner = {}
)

private val fastOutLinearIn = FastOutLinearInInterpolator()
