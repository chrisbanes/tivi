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

import android.os.Build
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
import androidx.ui.core.Alignment
import androidx.ui.core.ConfigurationAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.ProvideContentColor
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.shape.RectangleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.ScaleFit
import androidx.ui.graphics.withSave
import androidx.ui.layout.Column
import androidx.ui.layout.ColumnAlign
import androidx.ui.layout.Row
import androidx.ui.layout.RowAlign
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredSizeIn
import androidx.ui.material.Button
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Surface
import androidx.ui.material.ripple.ripple
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
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.LoadNetworkImageWithCrossfade
import app.tivi.common.compose.MaterialThemeFromAndroidTheme
import app.tivi.common.compose.SwipeDirection
import app.tivi.common.compose.SwipeToDismiss
import app.tivi.common.compose.TiviAlertDialog
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.WrapWithAmbients
import app.tivi.common.compose.boundsInParent
import app.tivi.common.compose.center
import app.tivi.common.compose.observe
import app.tivi.common.compose.observeInsets
import app.tivi.common.compose.paddingHV
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
                        Spacer(modifier = Modifier.preferredHeight(8.dp))

                        if (watches.isEmpty()) {
                            MarkWatchedButton(
                                modifier = Modifier.gravity(ColumnAlign.Center),
                                actioner = actioner
                            )
                        } else {
                            AddWatchButton(
                                modifier = Modifier.gravity(ColumnAlign.Center),
                                actioner = actioner
                            )
                        }
                    }

                    Spacer(modifier = Modifier.preferredHeight(16.dp))

                    if (watches.isNotEmpty()) {
                        var openDialog by state { false }

                        EpisodeWatchesHeader(onSweepWatchesClick = { openDialog = true })

                        if (openDialog) {
                            TiviAlertDialog(
                                title = stringResource(R.string.episode_remove_watches_dialog_title),
                                message = stringResource(R.string.episode_remove_watches_dialog_message),
                                confirmText = stringResource(R.string.episode_remove_watches_dialog_confirm),
                                onConfirm = {
                                    actioner(RemoveAllEpisodeWatchesAction)
                                    openDialog = false
                                },
                                dismissText = stringResource(R.string.dialog_dismiss),
                                onDismiss = { openDialog = false }
                            )
                        }
                    }

                    watches.forEach { watch ->
                        SwipeToDismiss(
                            swipeDirections = listOf(SwipeDirection.START),
                            onSwipeComplete = {
                                actioner(RemoveEpisodeWatchAction(watch.id))
                            },
                            swipeChildren = { _, _ -> EpisodeWatch(episodeWatchEntry = watch) },
                            backgroundChildren = { swipeProgress, completeOnRelease ->
                                EpisodeWatchSwipeBackground(
                                    swipeProgress = swipeProgress,
                                    wouldCompleteOnRelease = completeOnRelease
                                )
                            }
                        )
                    }

                    with(DensityAmbient.current) {
                        Spacer(
                            modifier = Modifier.preferredHeight(InsetsAmbient.current.bottom.toDp())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Backdrop(season: Season, episode: Episode) {
    Surface(modifier = Modifier.aspectRatio(16f / 10)) {
        Stack {
            if (episode.tmdbBackdropPath != null) {
                LoadNetworkImageWithCrossfade(
                    modifier = Modifier.matchParent(),
                    data = episode,
                    scaleFit = ScaleFit.FillMaxDimension
                )
            }

            Column(
                modifier = Modifier.gravity(Alignment.BottomStart)
                    .drawBackground(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(topRight = 8.dp)
                    )
                    .padding(all = 16.dp)
            ) {
                val type = MaterialTheme.typography
                val epNumber = episode.number
                val seasonNumber = season.number

                ProvideContentColor(color = Color.White) {
                    if (seasonNumber != null && epNumber != null) {
                        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                            @Suppress("DEPRECATION")
                            val locale = ConfigurationAmbient.current.locale
                            Text(
                                text = stringResource(R.string.season_episode_number,
                                    seasonNumber, epNumber).toUpperCase(locale),
                                style = MaterialTheme.typography.overline
                                    .copy(color = contentColor())
                            )
                        }
                        Spacer(modifier = Modifier.preferredHeight(4.dp))
                    }

                    ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
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
                modifier = Modifier.weight(1f),
                iconResId = R.drawable.ic_details_rating,
                label = stringResource(R.string.trakt_rating_text, rating * 10f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = TiviDateFormatterAmbient.current
            InfoPane(
                modifier = Modifier.weight(1f),
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
    Column(modifier = modifier.padding(all = 16.dp)) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            VectorImage(
                modifier = Modifier.gravity(ColumnAlign.Center),
                id = iconResId
            )
        }

        Spacer(modifier = Modifier.preferredHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.gravity(ColumnAlign.Center),
                text = label,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun Summary(episode: Episode) {
    var canExpand by stateFor(episode) { true }

    Box(modifier = Modifier.ripple(bounded = true, enabled = canExpand)) {
        var expanded by state { false }

        Clickable(onClick = { expanded = !expanded }) {
            ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = episode.summary ?: "No summary",
                    style = MaterialTheme.typography.body2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (expanded) Int.MAX_VALUE else 4
                ) {
                    if (!expanded) {
                        canExpand = it.hasVisualOverflow
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeWatchesHeader(onSweepWatchesClick: () -> Unit) {
    Row {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.paddingHV(horizontal = 16.dp, vertical = 8.dp)
                    .gravity(RowAlign.Center)
                    .weight(1f),
                text = stringResource(R.string.episode_watches),
                style = MaterialTheme.typography.subtitle1
            )
        }

        ProvideEmphasis(EmphasisAmbient.current.disabled) {
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = { onSweepWatchesClick() }
            ) {
                VectorImage(id = R.drawable.ic_delete_sweep_24)
            }
        }
    }
}

@Composable
private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Surface {
        Row(
            modifier = Modifier.paddingHV(horizontal = 16.dp, vertical = 8.dp)
                .preferredSizeIn(minWidth = 40.dp, minHeight = 40.dp)
        ) {
            ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                val formatter = TiviDateFormatterAmbient.current
                Text(
                    modifier = Modifier.weight(1f).gravity(RowAlign.Center),
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = MaterialTheme.typography.body2
                )
            }

            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                    VectorImage(
                        R.drawable.ic_upload_24dp,
                        modifier = Modifier.padding(start = 8.dp).gravity(RowAlign.Center)
                    )
                }

                if (episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                    VectorImage(
                        R.drawable.ic_eye_off_24dp,
                        modifier = Modifier.padding(start = 8.dp).gravity(RowAlign.Center)
                    )
                }
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
    val secondary = MaterialTheme.colors.error.copy(alpha = 0.5f)
    val default = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)

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
            Modifier.fillMaxSize()
                .drawBackground(MaterialTheme.colors.onSurface.copy(alpha = 0.2f), RectangleShape)
        ) {
            // A simple box to draw the growing circle, which emanates from behind the icon
            Box(
                modifier = Modifier.fillMaxSize() +
                    CircleGrowDrawModifier(
                        transitionState[color],
                        iconCenter.toOffset(),
                        lerp(0f, maxRadius.toFloat(), fastOutLinearIn(swipeProgress))
                    )
            )

            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                VectorImage(
                    id = R.drawable.ic_delete_24,
                    modifier = Modifier.onPositioned { iconCenter = it.boundsInParent.center }
                        .padding(0.dp, 0.dp, end = 16.dp, bottom = 0.dp)
                        .gravity(Alignment.CenterEnd)
                )
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
    Button(
        modifier = modifier,
        elevation = if (Build.VERSION.SDK_INT != 28) 2.dp else 0.dp, // b/152696056
        onClick = { actioner(AddEpisodeWatchAction) }
    ) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                text = stringResource(R.string.episode_mark_watched),
                style = MaterialTheme.typography.button.copy(color = contentColor())
            )
        }
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
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                text = stringResource(R.string.episode_add_watch),
                style = MaterialTheme.typography.button.copy(color = contentColor())
            )
        }
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
