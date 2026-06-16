package com.nikolayux.masterchariot.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nikolayux.masterchariot.MainActivity
import com.nikolayux.masterchariot.R
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.math.abs

@Singleton
class MaintenanceNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationSettings: NotificationSettings
) {

    fun notifyIfNeeded(previousRemainingKm: Int, currentRemainingKm: Int) {
        val threshold = crossedMaintenanceThreshold(
            previousRemainingKm = previousRemainingKm,
            currentRemainingKm = currentRemainingKm
        ) ?: return

        showMaintenanceNotification(threshold)
    }

    private fun crossedMaintenanceThreshold(
        previousRemainingKm: Int,
        currentRemainingKm: Int
    ): Int? {
        if (currentRemainingKm > 500 || previousRemainingKm <= currentRemainingKm) {
            return null
        }

        val firstThreshold = if (previousRemainingKm > 500) {
            500
        } else {
            previousRemainingKm - positiveModulo(previousRemainingKm, 100)
        }

        var threshold = firstThreshold
        while (threshold >= currentRemainingKm) {
            if (previousRemainingKm > threshold) {
                return threshold
            }
            threshold -= 100
        }
        return null
    }

    private fun showMaintenanceNotification(thresholdKm: Int) {
        if (!notificationSettings.areMaintenanceNotificationsEnabled()) return
        if (!canPostNotifications()) return

        createChannel()

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (thresholdKm == 500) {
            context.getString(R.string.maintenance_notification_soon_text)
        } else {
            context.getString(R.string.maintenance_notification_urgent_text)
        }

        val notification = NotificationCompat.Builder(context, AppNotificationChannels.MAINTENANCE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.maintenance_notification_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(
            MAINTENANCE_NOTIFICATION_ID_BASE + abs(thresholdKm),
            notification
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            AppNotificationChannels.MAINTENANCE,
            context.getString(R.string.maintenance_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun positiveModulo(value: Int, modulo: Int): Int {
        return ((value % modulo) + modulo) % modulo
    }

    private companion object {
        const val MAINTENANCE_NOTIFICATION_ID_BASE = 20_000
    }
}
