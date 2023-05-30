// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.tmdb.TmdbComponent
import app.tivi.trakt.TraktComponent

interface ApiComponent : TmdbComponent, TraktComponent
