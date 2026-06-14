package com.nikolayux.masterchariot.data.trip.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nikolayux.masterchariot.MainActivity
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.data.notification.AppNotificationChannels
import com.nikolayux.masterchariot.data.trip.TripTracker
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class TripTrackingService : Service() {

    @Inject
    lateinit var tripTracker: TripTracker

    @Inject
    lateinit var bluetoothService: BluetoothService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(TRIP_TRACKING_NOTIFICATION_ID, buildNotification())

        serviceScope.launch {
            bluetoothService.connectionState.collect { status ->
                if (status == ConnectionStatus.Disconnected || status == ConnectionStatus.Error) {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tripTracker.start()
        return START_STICKY
    }

    override fun onDestroy() {
        runBlocking(Dispatchers.IO) {
            tripTracker.stop()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification() = NotificationCompat.Builder(this, AppNotificationChannels.TRIP_TRACKING)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(getString(R.string.trip_tracking_notification_title))
        .setContentText(getString(R.string.trip_tracking_notification_text))
        .setContentIntent(contentIntent())
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            AppNotificationChannels.TRIP_TRACKING,
            getString(R.string.trip_tracking_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val TRIP_TRACKING_NOTIFICATION_ID = 10_001

        fun start(context: Context) {
            val intent = Intent(context, TripTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TripTrackingService::class.java))
        }
    }
}
