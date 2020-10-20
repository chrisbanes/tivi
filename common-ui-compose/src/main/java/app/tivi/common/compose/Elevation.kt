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

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.common.compose

import androidx.compose.foundation.AmbientIndication
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSizeConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonConstants
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ElevationOverlay
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

val AmbientAbsoluteElevation = staticAmbientOf { 0.dp }

/**
 * A wrapper around [Surface] which works with absolute elevation. This will automatically
 * update [AmbientAbsoluteElevation], adding [elevation].
 */
@Composable
fun AbsoluteElevationSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val currentAbsElevation = AmbientAbsoluteElevation.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    ) {
        Providers(AmbientAbsoluteElevation provides currentAbsElevation + elevation) {
            content()
        }
    }
}

/**
 * An [ElevationOverlay] which supports absolute elevation, taking the current
 * [AmbientAbsoluteElevation] into account.
 */
object AbsoluteElevationOverlay : ElevationOverlay {
    @Composable
    override fun apply(color: Color, elevation: Dp): Color {
        val colors = MaterialTheme.colors
        val absElevation = AmbientAbsoluteElevation.current + elevation
        return if (absElevation > 0.dp && !colors.isLight) {
            val foregroundColor = calculateOverlayColor(contentColorFor(color), absElevation)
            foregroundColor.compositeOver(color)
        } else color
    }
}

private fun calculateOverlayColor(foregroundColor: Color, elevation: Dp): Color {
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return foregroundColor.copy(alpha = alpha)
}

/**
 * Version of [androidx.compose.material.Button] which supports absolute elevation.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AbsoluteElevationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionState: InteractionState = remember { InteractionState() },
    elevation: ButtonElevation? = ButtonConstants.defaultElevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonConstants.defaultButtonColors(),
    contentPadding: PaddingValues = ButtonConstants.DefaultContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    // TODO(aelias): Avoid manually putting the clickable above the clip and
    // the ripple below the clip once http://b/157687898 is fixed and we have
    // more flexibility to move the clickable modifier (see candidate approach
    // aosp/1361921)
    AbsoluteElevationSurface(
        shape = shape,
        color = colors.backgroundColor(enabled),
        contentColor = colors.contentColor(enabled),
        border = border,
        elevation = elevation?.elevation(enabled, interactionState) ?: 0.dp,
        modifier = modifier.clickable(
            onClick = onClick,
            enabled = enabled,
            interactionState = interactionState,
            indication = null
        )
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.button) {
            Row(
                Modifier
                    .defaultMinSizeConstraints(
                        minWidth = ButtonConstants.DefaultMinWidth,
                        minHeight = ButtonConstants.DefaultMinHeight
                    )
                    .indication(interactionState, AmbientIndication.current())
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                children = content
            )
        }
    }
}

/**
 * Version of [androidx.compose.material.OutlinedButton] which supports absolute elevation.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun AbsoluteElevationOutlinedButton(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionState: InteractionState = remember { InteractionState() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = ButtonConstants.defaultOutlinedBorder,
    colors: ButtonColors = ButtonConstants.defaultOutlinedButtonColors(),
    contentPadding: PaddingValues = ButtonConstants.DefaultContentPadding,
    noinline content: @Composable RowScope.() -> Unit
) = AbsoluteElevationButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionState = interactionState,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = content
)

/**
 * Version of [androidx.compose.material.TextButton] which supports absolute elevation.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun AbsoluteElevationTextButton(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionState: InteractionState = remember { InteractionState() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonConstants.defaultTextButtonColors(),
    contentPadding: PaddingValues = ButtonConstants.DefaultTextContentPadding,
    noinline content: @Composable RowScope.() -> Unit
) = AbsoluteElevationButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionState = interactionState,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = content
)

/**
 * Version of [androidx.compose.material.Card] which supports absolute elevation.
 */
@Composable
inline fun AbsoluteElevationCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    noinline content: @Composable () -> Unit
) {
    AbsoluteElevationSurface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        border = border,
        content = content
    )
}
