/*
 * Copyright 2021 Google LLC
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

package app.tivi.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Simple clone of FlowRow that arranges its children in a horizontal flow with limited
 * customization.
 */
@Composable
fun SimpleFlowRow(
    mainAxisArrangement: Arrangement.Horizontal = Arrangement.Start,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val childConstraints = Constraints(maxWidth = constraints.maxWidth)

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(childConstraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = if (constraints.maxWidth != Constraints.Infinity) {
            constraints.maxWidth
        } else {
            max(mainAxisSpace, constraints.minWidth)
        }
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        val layoutWidth = mainAxisLayoutSize
        val layoutHeight = crossAxisLayoutSize

        layout(layoutWidth, layoutHeight) {
            for (i in sequences.indices) {
                val placeables = sequences[i]
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                        if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                // Handle vertical direction
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }

                with(mainAxisArrangement) {
                    arrange(
                        totalSize = mainAxisLayoutSize,
                        sizes = childrenMainAxisSizes,
                        layoutDirection = layoutDirection,
                        outPositions = mainAxisPositions
                    )
                }

                for (j in placeables.indices) {
                    val placeable = placeables[j]
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}
