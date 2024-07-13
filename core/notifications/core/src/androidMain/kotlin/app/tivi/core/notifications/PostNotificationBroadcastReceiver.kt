// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostNotificationBroadcastReceiver : BroadcastReceiver() {

  @OptIn(DelicateCoroutinesApi::class)
  @SuppressLint("MissingPermission")
  override fun onReceive(context: Context, intent: Intent) {
    Log.d(TAG, "Received intent: %s".format(intent))

    val id = intent.getStringExtra(EXTRA_ID)
    if (id == null) {
      Log.d(TAG, "ID not provided. Exiting.")
      return
    }

    val notificationManager = NotificationManagerCompat.from(context)
    val store = context.pendingNotificationsStore
    val result = goAsync()

    GlobalScope.launch {
      val pending = store.findWithId(id) ?: run {
        Log.d(TAG, "Pending Notification with ID: $id not found. Exiting.")
        result.finish()
        return@launch
      }

      Log.d(TAG, "Found pending notification with ID: $id: $pending")

      val notification = NotificationCompat.Builder(context, pending.channel.id)
        // Replace this icon with something better
        .setSmallIcon(R.drawable.outline_tv_gen_24)
        .setContentTitle(pending.title)
        .setContentText(pending.message)
        .apply {
          if (pending.deeplinkUrl != null) {
            setContentIntent(
              PendingIntent.getActivity(
                context,
                0,
                Intent(Intent.ACTION_VIEW, Uri.parse(pending.deeplinkUrl)).apply {
                  flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
              ),
            )
          }
        }
        .build()

      try {
        notificationManager.notify(id, 0, notification)
      } catch (se: SecurityException) {
        Log.d(TAG, "Error posting notification", se)
      } finally {
        store.removeWithId(id)
      }

      result.finish()
    }
  }

  companion object {
    private const val TAG = "PostNotificationBroadcastReceiver"

    private const val EXTRA_ID = "notification_id"

    fun buildIntent(context: Context, id: String): Intent {
      return Intent(context, PostNotificationBroadcastReceiver::class.java)
        .putExtra(EXTRA_ID, id)
    }
  }
}
