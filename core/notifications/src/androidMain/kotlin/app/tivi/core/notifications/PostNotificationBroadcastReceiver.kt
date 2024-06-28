/// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PostNotificationBroadcastReceiver : BroadcastReceiver() {

  @SuppressLint("MissingPermission")
  override fun onReceive(context: Context, intent: Intent) {
    val notificationManager = NotificationManagerCompat.from(context)

    val id = intent.getStringExtra(EXTRA_ID)
    val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
      ?: error("Value for EXTRA_CHANNEL_ID not provided")
    val title = intent.getStringExtra(EXTRA_TITLE)
    val content = intent.getStringExtra(EXTRA_CONTENT)

    val notification = NotificationCompat.Builder(context, channelId)
      // Replace this with something better
      .setSmallIcon(R.drawable.outline_tv_gen_24)
      .setContentTitle(title)
      .setContentText(content)
      .build()
    try {
      notificationManager.notify(id, 0, notification)
    } catch (se: SecurityException) {
      Log.d("PostNotificationBroadcastReceiver", "Error whilst posting notification", se)
    }
  }

  companion object {
    private const val EXTRA_ID = "notification_id"
    private const val EXTRA_CHANNEL_ID = "notification_channel_id"
    private const val EXTRA_TITLE = "notification_title"
    private const val EXTRA_CONTENT = "notification_content"

    fun buildIntent(
      context: Context,
      id: String,
      channelId: String,
      title: String,
      text: String,
    ): Intent = Intent(context, PostNotificationBroadcastReceiver::class.java)
      .putExtra(EXTRA_ID, id)
      .putExtra(EXTRA_CHANNEL_ID, channelId)
      .putExtra(EXTRA_TITLE, title)
      .putExtra(EXTRA_CONTENT, text)
  }
}
