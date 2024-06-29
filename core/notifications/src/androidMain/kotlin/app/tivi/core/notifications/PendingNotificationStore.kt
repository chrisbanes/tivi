// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import app.tivi.core.notifications.proto.PendingNotifications as PendingNotificationsProto
import com.squareup.wire.Instant as WireInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Instant as KotlinInstant
import kotlinx.datetime.toJavaInstant
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path

internal fun pendingNotificationsStore(
  coroutineScope: CoroutineScope,
  path: () -> Path,
): DataStore<PendingNotificationsProto> = DataStoreFactory.create(
  storage = OkioStorage(
    fileSystem = FileSystem.SYSTEM,
    serializer = object : OkioSerializer<PendingNotificationsProto> {
      override val defaultValue: PendingNotificationsProto get() = PendingNotificationsProto()

      override suspend fun readFrom(source: BufferedSource): PendingNotificationsProto {
        return try {
          PendingNotificationsProto.ADAPTER.decode(source)
        } catch (exception: IOException) {
          throw CorruptionException("Cannot read proto", exception)
        }
      }

      override suspend fun writeTo(t: PendingNotificationsProto, sink: BufferedSink) {
        PendingNotificationsProto.ADAPTER.encode(sink, t)
      }
    },
    producePath = path,
  ),
  corruptionHandler = null,
  scope = coroutineScope,
)

fun KotlinInstant.toWireInstant(): WireInstant = toJavaInstant()
