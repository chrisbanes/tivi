/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class TraktUser(
        @PrimaryKey @ColumnInfo(name = "id") var id: Long? = null,
        @ColumnInfo(name = "username") var username: String? = null,
        @ColumnInfo(name = "name") var name: String? = null,
        @ColumnInfo(name = "joined_date") var joined: Date? = null,
        @ColumnInfo(name = "location") var location: String? = null,
        @ColumnInfo(name = "about") var about: String? = null,
        @ColumnInfo(name = "avatar_url") var avatarUrl: String? = null)
