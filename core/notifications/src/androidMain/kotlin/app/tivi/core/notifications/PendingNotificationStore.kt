// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import app.tivi.core.notifications.proto.PendingNotification
import app.tivi.core.notifications.proto.PendingNotification as PendingNotificationProto
import app.tivi.core.notifications.proto.PendingNotifications as PendingNotificationsProto
import com.squareup.wire.Instant as WireInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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

suspend fun DataStore<PendingNotificationsProto>.add(pending: PendingNotificationProto) {
  updateData { data ->
    data.copy(pending = (data.pending + pending).asReversed().distinctBy(PendingNotification::id))
  }
}

suspend fun DataStore<PendingNotificationsProto>.findWithId(id: String): PendingNotificationProto? {
  return data.first().pending.firstOrNull { it.id == id }
}

suspend fun DataStore<PendingNotificationsProto>.removeWithId(id: String) {
  updateData { data ->
    data.copy(
      data.pending.filterNot { it.id == id },
    )
  }
}

interface PendingNotificationsStoreProvider {
  val pendingNotificationsStore: DataStore<PendingNotificationsProto>
}

val Context.pendingNotificationsStore: DataStore<PendingNotificationsProto>
  get() = (applicationContext as PendingNotificationsStoreProvider).pendingNotificationsStore
