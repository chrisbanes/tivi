// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.app.Application
import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.dataStoreFile
import app.tivi.core.notifications.proto.PendingNotification
import app.tivi.core.notifications.proto.PendingNotification as PendingNotificationProto
import app.tivi.core.notifications.proto.PendingNotifications as PendingNotificationsProto
import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import me.tatarka.inject.annotations.Inject
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath

@ApplicationScope
@Inject
class PendingNotificationStore(
  application: Application,
  dispatchers: AppCoroutineDispatchers,
) {
  private val scope = CoroutineScope(dispatchers.io + SupervisorJob()) // Inject this

  private val store by lazy {
    pendingNotificationsStore(scope) {
      application.dataStoreFile("pending_notifications.pb")
        .absoluteFile
        .toOkioPath()
    }
  }

  suspend fun findWithId(id: String): Notification? {
    return store.data.first().pending.firstOrNull { it.id == id }?.toNotification()
  }

  suspend fun add(pending: PendingNotificationProto) {
    store.updateData { data ->
      data.copy(
        pending = (data.pending + pending).asReversed().distinctBy(PendingNotificationProto::id),
      )
    }
  }

  suspend fun removeWithId(id: String) {
    store.updateData { data ->
      data.copy(data.pending.filterNot { it.id == id })
    }
  }

  suspend fun getPendingNotifications(): List<Notification> {
    // First we remove any old (more than 1 day in past) pending notifications
    store.updateData { data ->
      val filtered = data.pending.filter {
        val date = it.date?.toKotlinInstant()
        date != null && date > (Clock.System.now() - 1.days)
      }
      data.copy(filtered)
    }

    return store.data.firstOrNull()
      ?.pending
      ?.map(PendingNotification::toNotification)
      ?: emptyList()
  }
}

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

interface PendingNotificationsStoreProvider {
  val pendingNotificationsStore: PendingNotificationStore
}

val Context.pendingNotificationsStore: PendingNotificationStore
  get() = (applicationContext as PendingNotificationsStoreProvider).pendingNotificationsStore

internal fun PendingNotificationProto.toNotification(): Notification {
  return Notification(
    id = id,
    title = title,
    message = message,
    deeplinkUrl = deeplink_url,
    date = date?.toKotlinInstant() ?: Instant.DISTANT_PAST,
    channel = NotificationChannel.fromId(channel_id),
  )
}

internal fun Notification.toPendingNotification(): PendingNotificationProto {
  return PendingNotificationProto(
    id = id,
    title = title,
    message = message,
    deeplink_url = deeplinkUrl,
    date = date.toJavaInstant(),
    channel_id = channel.id,
  )
}
