// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class TraktUser(
    override val id: Long = 0,
    val username: String,
    val name: String? = null,
    val joined: Instant? = null,
    val location: String? = null,
    val about: String? = null,
    val avatarUrl: String? = null,
    val vip: Boolean? = null,
    val isMe: Boolean = false,
) : TiviEntity
