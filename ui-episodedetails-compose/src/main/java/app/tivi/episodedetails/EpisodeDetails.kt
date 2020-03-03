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
import androidx.animation.transitionDefinition
import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.compose.stateFor
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.ui.animation.ColorPropKey
import androidx.ui.animation.Transition
import androidx.ui.core.DensityAmbient
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.OnChildPositioned
import androidx.ui.core.Text
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.ProvideContentColor
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.contentColor
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.withSave
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutAspectRatio
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.material.EmphasisLevels
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
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
import app.tivi.common.compose.GradientScrimDrawModifier
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.LoadNetworkImageWithCrossfade
import app.tivi.common.compose.MaterialThemeFromAndroidTheme
import app.tivi.common.compose.SwipeDirection
import app.tivi.common.compose.SwipeToDismiss
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.WrapWithAmbients
import app.tivi.common.compose.boundsInParent
import app.tivi.common.compose.center
import app.tivi.common.compose.observe
import app.tivi.common.compose.observeInsets
import app.tivi.common.compose.setContentWithLifecycle
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.episodedetails.compose.R
import app.tivi.ui.animations.lerp
import app.tivi.util.TiviDateFormatter
import kotlin.math.hypot
import org.threeten.bp.OffsetDateTime

/**
 * This is a bit of hack. I can't make `ui-episodedetails` depend on any of the compose libraries,
 * so I wrap `setContext` as my own function, which `ui-episodedetails` can use.
 *
 * We need to return an `Any` since this method will be called from modules which do not depend
 * on Compose
 */
fun ViewGroup.composeEpisodeDetails(
    lifecycleOwner: LifecycleOwner,
    state: LiveData<EpisodeDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (EpisodeDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = setContentWithLifecycle(lifecycleOwner) {
    WrapWithAmbients(tiviDateFormatter, InsetsHolder()) {
        observeInsets(insets)

        val viewState = observe(state)
        if (viewState != null) {
            MaterialThemeFromAndroidTheme(context) {
                EpisodeDetails(viewState, actioner)
            }
        }
    }
}

@Composable
private fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Column {
        if (viewState.episode != null && viewState.season != null) {
            Backdrop(season = viewState.season!!, episode = viewState.episode!!)
        }
        VerticalScroller {
            Surface(elevation = 2.dp) {
                Column {
                    val episode = viewState.episode
                    if (episode != null) {
                        InfoPanes(episode)
                        Summary(episode)
                    }

                    val watches = viewState.watches

                    if (viewState.canAddEpisodeWatch) {
                        if (watches.isEmpty()) {
                            MarkWatchedButton(modifier = LayoutGravity.Center, actioner = actioner)
                        } else {
                            AddWatchButton(modifier = LayoutGravity.Center, actioner = actioner)
                        }
                    }

                    Spacer(modifier = LayoutHeight(16.dp))

                    if (watches.isNotEmpty()) {
                        EpisodeWatchesHeader()
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
                                EpisodeWatchSwipeBackground(
                                    swipeProgress = swipeProgress,
                                    wouldCompleteOnRelease = completeOnRelease
                                )
                            }
                        )
                    }

                    with(DensityAmbient.current) {
                        Spacer(modifier = LayoutHeight(InsetsAmbient.current.bottom.toDp()))
                    }
                }
            }
        }
    }
}

@Composable
private fun Backdrop(season: Season, episode: Episode) {
    Surface(modifier = LayoutAspectRatio(16f / 10)) {
        Stack {
            if (episode.tmdbBackdropPath != null) {
                LoadNetworkImageWithCrossfade(modifier = LayoutGravity.Stretch, data = episode)
            }

            Box(modifier = LayoutGravity.Stretch +
                GradientScrimDrawModifier(baseColor = Color.Black))

            Column(modifier = LayoutPadding(all = 16.dp) + LayoutGravity.BottomStart) {
                val type = MaterialTheme.typography()
                val epNumber = episode.number
                val seasonNumber = season.number

                ProvideContentColor(color = Color.White) {
                    if (seasonNumber != null && epNumber != null) {
                        ProvideEmphasis(emphasis = EmphasisLevels().medium) {
                            Text(
                                text = stringResource(R.string.season_episode_number,
                                    seasonNumber, epNumber).toUpperCase(),
                                style = MaterialTheme.typography().overline.copy(color = contentColor())
                            )
                        }
                        Spacer(modifier = LayoutHeight(4.dp))
                    }

                    ProvideEmphasis(emphasis = EmphasisLevels().high) {
                        Text(
                            text = episode.title ?: "No title",
                            style = type.h6.copy(color = contentColor())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanes(episode: Episode) {
    Row {
        episode.traktRating?.let { rating ->
            InfoPane(
                modifier = LayoutFlexible(1f),
                iconResId = R.drawable.ic_details_rating,
                label = stringResource(R.string.trakt_rating_text, rating * 10f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = TiviDateFormatterAmbient.current
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
    Column(modifier = modifier + LayoutPadding(all = 16.dp)) {
        ProvideEmphasis(emphasis = EmphasisLevels().medium) {
            VectorImage(
                modifier = LayoutGravity.Center,
                id = iconResId
            )
        }

        Spacer(modifier = LayoutHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisLevels().high) {
            Text(
                modifier = LayoutGravity.Center,
                text = label,
                style = MaterialTheme.typography().body1
            )
        }
    }
}

@Composable
private fun Summary(episode: Episode) {
    var canExpand by stateFor(episode) { true }

    Ripple(bounded = true, enabled = canExpand) {
        Container {
            var expanded by state { false }

            Clickable(onClick = { expanded = !expanded }) {
                ProvideEmphasis(emphasis = EmphasisLevels().high) {
                    Text(
                        modifier = LayoutPadding(16.dp),
                        text = episode.summary ?: "No summary",
                        style = MaterialTheme.typography().body2,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = if (expanded) Int.MAX_VALUE else 4
                    ) {
                        if (!expanded) {
                            // TODO: this is currently always false /shruggie
                            // canExpand = it.hasVisualOverflow
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeWatchesHeader() {
    ProvideEmphasis(emphasis = EmphasisLevels().high) {
        Text(
            modifier = LayoutPadding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            text = stringResource(R.string.episode_watches),
            style = MaterialTheme.typography().subtitle1
        )
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
                val formatter = TiviDateFormatterAmbient.current
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
                        modifier = LayoutPadding(start = 8.dp) + LayoutGravity.Center
                    )
                }

                VectorImage(
                    id = when (episodeWatchEntry.pendingAction) {
                        PendingAction.DELETE -> R.drawable.ic_eye_off_24dp
                        else -> R.drawable.ic_eye_24dp
                    },
                    modifier = LayoutPadding(start = 8.dp) + LayoutGravity.Center
                )
            }
        }
    }
}

private val color = ColorPropKey()

@Composable
private fun EpisodeWatchSwipeBackground(
    swipeProgress: Float,
    wouldCompleteOnRelease: Boolean = false
) {
    var iconCenter by state { PxPosition(Px.Zero, Px.Zero) }

    val maxRadius = remember(iconCenter) {
        hypot(iconCenter.x.value.toDouble(), iconCenter.y.value.toDouble())
    }

    // Note: can't reference these directly in transitionDefinition {} as
    // it's not @Composable
    val secondary = MaterialTheme.colors().error.copy(alpha = 0.5f)
    val default = MaterialTheme.colors().onSurface.copy(alpha = 0.2f)

    val transition = remember(secondary, default) {
        transitionDefinition {
            state(true) {
                this[color] = secondary
            }
            state(false) {
                this[color] = default
            }

            transition {
                color using tween { duration = 200 }
            }
        }
    }

    Transition(
        definition = transition,
        toState = wouldCompleteOnRelease
    ) { transitionState ->
        Stack(
            modifier = LayoutSize.Fill +
                DrawBackground(MaterialTheme.colors().onSurface.copy(alpha = 0.2f))
        ) {
            // A simple box to draw the growing circle, which emanates from behind the icon
            Box(
                modifier = LayoutSize.Fill +
                    CircleGrowDrawModifier(
                        transitionState[color],
                        iconCenter.toOffset(),
                        lerp(0f, maxRadius.toFloat(), fastOutLinearIn(swipeProgress))
                    )
            )

            OnChildPositioned(onPositioned = { iconCenter = it.boundsInParent.center }) {
                ProvideEmphasis(emphasis = EmphasisLevels().medium) {
                    VectorImage(
                        id = R.drawable.ic_delete_24,
                        modifier = LayoutPadding(end = 16.dp) + LayoutGravity.CenterEnd
                    )
                }
            }
        }
    }
}

private fun CircleGrowDrawModifier(
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
        drawContent()

        canvas.withSave {
            canvas.clipRect(size.toRect())
            canvas.drawCircle(centerPoint, radius, paint)
        }
    }
}

@Composable
fun MarkWatchedButton(
    modifier: Modifier = Modifier.None,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    ProvideEmphasis(EmphasisLevels().high) {
        FloatingActionButton(
            modifier = modifier,
            color = MaterialTheme.colors().secondary,
            text = stringResource(R.string.episode_mark_watched),
            textStyle = MaterialTheme.typography().button
                .copy(color = MaterialTheme.colors().onSecondary),
            onClick = { actioner(AddEpisodeWatchAction) }
        )
    }
}

@Composable
fun AddWatchButton(
    modifier: Modifier = Modifier.None,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = { actioner(AddEpisodeWatchAction) }
    ) {
        Text(text = stringResource(R.string.episode_add_watch))
    }
}

@Preview
@Composable
fun previewEpisodeDetails() = EpisodeDetails(
    viewState = EpisodeDetailsViewState(
        episodeId = 0,
        episode = Episode(
            seasonId = 100,
            title = "A show too far",
            summary = "A long description of a episode",
            traktRating = 0.5f,
            traktRatingVotes = 84,
            firstAired = OffsetDateTime.now()
        ),
        season = Season(
            id = 100,
            showId = 0
        ),
        watches = listOf(
            EpisodeWatchEntry(
                id = 10,
                episodeId = 100,
                watchedAt = OffsetDateTime.now()
            )
        )
    ),
    actioner = {}
)

private val fastOutLinearIn = FastOutLinearInInterpolator()
