// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.screens

/**
 * Copied from https://github.com/realityexpander/NoteAppKMM
 */

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual typealias CommonParcelize = kotlinx.parcelize.Parcelize

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual typealias CommonParceler<T> = kotlinx.parcelize.Parceler<T>

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual typealias CommonTypeParceler<T, P> = kotlinx.parcelize.TypeParceler<T, P>
