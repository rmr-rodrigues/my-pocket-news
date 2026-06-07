package com.mypocketnews.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager

fun createChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    nm.createNotificationChannel(
        NotificationChannel("processing", "Article processing", NotificationManager.IMPORTANCE_LOW)
    )
    nm.createNotificationChannel(
        NotificationChannel("completion", "Article ready", NotificationManager.IMPORTANCE_DEFAULT)
    )
    nm.createNotificationChannel(
        NotificationChannel("error", "Processing error", NotificationManager.IMPORTANCE_DEFAULT)
    )
}

class AppNotifier {

    fun buildProcessingNotification(context: Context): Notification {
        checkPermission(context)
        return NotificationCompat.Builder(context, "processing")
            .setContentTitle("My Pocket News")
            .setContentText("Processing article…")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .build()
    }

    fun postCompletion(context: Context, articleTitle: String, notificationId: Int) {
        if (!checkPermission(context)) return
        NotificationManagerCompat.from(context).apply {
            cancel(notificationId)
            notify(
                notificationId,
                NotificationCompat.Builder(context, "completion")
                    .setContentTitle("Article saved")
                    .setContentText(articleTitle)
                    .setSmallIcon(android.R.drawable.ic_popup_sync)
                    .build()
            )
        }
    }

    fun postError(context: Context, errorMessage: String, notificationId: Int) {
        if (!checkPermission(context)) return
        NotificationManagerCompat.from(context).notify(
            notificationId,
            NotificationCompat.Builder(context, "error")
                .setContentTitle("Could not save article")
                .setContentText(errorMessage.take(80))
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .build()
        )
    }

    private fun checkPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
