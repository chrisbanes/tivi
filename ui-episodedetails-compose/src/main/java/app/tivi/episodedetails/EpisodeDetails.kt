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
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.Recomposer
import androidx.compose.getValue
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LiveData
import androidx.ui.animation.ColorPropKey
import androidx.ui.animation.Crossfade
import androidx.ui.animation.Transition
import androidx.ui.core.Alignment
import androidx.ui.core.ConfigurationAmbient
import androidx.ui.core.ContentDrawScope
import androidx.ui.core.ContentScale
import androidx.ui.core.DensityAmbient
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.RectangleShape
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.graphics.withSave
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredSizeIn
import androidx.ui.livedata.observeAsState
import androidx.ui.material.Button
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Snackbar
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.CalendarToday
import androidx.ui.material.icons.filled.Delete
import androidx.ui.material.icons.filled.DeleteSweep
import androidx.ui.material.icons.filled.Publish
import androidx.ui.material.icons.filled.Star
import androidx.ui.material.icons.filled.VisibilityOff
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Px
import androidx.ui.unit.PxPosition
import androidx.ui.unit.dp
import androidx.ui.unit.toOffset
import androidx.ui.unit.toRect
import app.tivi.animation.invoke
import app.tivi.common.compose.ExpandingSummary
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.LoadNetworkImageWithCrossfade
import app.tivi.common.compose.SwipeDirection
import app.tivi.common.compose.SwipeToDismiss
import app.tivi.common.compose.TiviAlertDialog
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.WrapWithAmbients
import app.tivi.common.compose.boundsInParent
import app.tivi.common.compose.center
import app.tivi.common.compose.observeInsets
import app.tivi.common.compose.paddingHV
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.episodedetails.compose.R
import app.tivi.ui.animations.lerp
import app.tivi.util.TiviDateFormatter
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme
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
    state: LiveData<EpisodeDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (EpisodeDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = setContent(Recomposer.current()) {
    WrapWithAmbients(tiviDateFormatter, InsetsHolder()) {
        MaterialThemeFromMdcTheme {
            observeInsets(insets)

            val viewState by state.observeAsState()
            if (viewState != null) {
                EpisodeDetails(viewState!!, actioner)
            }
        }
    }
}

@Composable
private fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Stack {
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
                            ExpandingSummary(
                                episode.summary ?: "No summary",
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        val watches = viewState.watches

                        if (viewState.canAddEpisodeWatch) {
                            Spacer(modifier = Modifier.preferredHeight(8.dp))

                            if (watches.isEmpty()) {
                                MarkWatchedButton(
                                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            } else {
                                AddWatchButton(
                                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            }
                        }

                        Spacer(modifier = Modifier.preferredHeight(16.dp))

                        if (watches.isNotEmpty()) {
                            var openDialog by state { false }

                            EpisodeWatchesHeader(
                                onSweepWatchesClick = { openDialog = true }
                            )

                            if (openDialog) {
                                RemoveAllWatchesDialog(
                                    actioner = actioner,
                                    onDialogClosed = { openDialog = false }
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

                        val bottomInset = with(DensityAmbient.current) {
                            InsetsAmbient.current.bottom.toDp()
                        }
                        Spacer(
                            modifier = Modifier.preferredHeight(bottomInset + 8.dp)
                        )
                    }
                }
            }
        }

        val bottomInset = with(DensityAmbient.current) {
            InsetsAmbient.current.bottom.toDp()
        }

        Column(
            modifier = Modifier.gravity(Alignment.BottomCenter)
                .padding(bottom = 16.dp + bottomInset)
        ) {
            Crossfade(current = viewState.error) { error ->
                if (error != null) {
                    // TODO: Convert this to swipe-to-dismiss
                    Clickable(onClick = { actioner(ClearError) }) {
                        Snackbar(
                            text = { Text(error.message) },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
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
                    episode,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
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

                Providers(ContentColorAmbient provides Color.White) {
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
                icon = Icons.Default.Star,
                label = stringResource(R.string.trakt_rating_text, rating * 10f),
                modifier = Modifier.weight(1f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = TiviDateFormatterAmbient.current
            InfoPane(
                icon = Icons.Default.CalendarToday,
                label = formatter.formatShortRelativeTime(firstAired),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoPane(
    modifier: Modifier = Modifier,
    icon: VectorAsset,
    label: String
) {
    Column(modifier = modifier.padding(all = 16.dp)) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Icon(
                asset = icon,
                modifier = Modifier.gravity(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.preferredHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.gravity(Alignment.CenterHorizontally),
                text = label,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun EpisodeWatchesHeader(onSweepWatchesClick: () -> Unit) {
    Row {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.paddingHV(horizontal = 16.dp, vertical = 8.dp)
                    .gravity(Alignment.CenterVertically)
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
                Icon(asset = Icons.Default.DeleteSweep)
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
                    modifier = Modifier.weight(1f).gravity(Alignment.CenterVertically),
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = MaterialTheme.typography.body2
                )
            }

            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                    Icon(
                        asset = Icons.Filled.Publish,
                        modifier = Modifier.padding(start = 8.dp).gravity(Alignment.CenterVertically)
                    )
                }

                if (episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                    Icon(
                        asset = Icons.Filled.VisibilityOff,
                        modifier = Modifier.padding(start = 8.dp).gravity(Alignment.CenterVertically)
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
                color using tween<Color> { duration = 200 }
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
                modifier = Modifier.fillMaxSize().drawGrowingCircle(
                        transitionState[color],
                        iconCenter.toOffset(),
                        lerp(0f, maxRadius.toFloat(), fastOutLinearIn(swipeProgress))
                    )
            )

            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                Icon(
                    asset = Icons.Default.Delete,
                    modifier = Modifier.onPositioned { iconCenter = it.boundsInParent.center }
                        .padding(0.dp, 0.dp, end = 16.dp, bottom = 0.dp)
                        .gravity(Alignment.CenterEnd)
                )
            }
        }
    }
}

private fun Modifier.drawGrowingCircle(
    color: Color,
    centerPoint: Offset,
    radius: Float
) = object : DrawModifier {
    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = color
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        withSave {
            clipRect(size.toRect())
            drawCircle(centerPoint, radius, paint)
        }
    }
}

@Composable
fun MarkWatchedButton(
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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

@Composable
private fun RemoveAllWatchesDialog(
    actioner: (EpisodeDetailsAction) -> Unit,
    onDialogClosed: () -> Unit
) {
    TiviAlertDialog(
        title = stringResource(R.string.episode_remove_watches_dialog_title),
        message = stringResource(R.string.episode_remove_watches_dialog_message),
        confirmText = stringResource(R.string.episode_remove_watches_dialog_confirm),
        onConfirm = {
            actioner(RemoveAllEpisodeWatchesAction)
            onDialogClosed()
        },
        dismissText = stringResource(R.string.dialog_dismiss),
        onDismiss = { onDialogClosed() }
    )
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
