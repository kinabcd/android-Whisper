package tw.lospot.kin.whisper.controller

import android.app.*
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import tw.lospot.kin.whisper.MainActivity
import tw.lospot.kin.whisper.R

class NotificationController(private val service: Service, private val callback: Listener) :
    Controller {
    companion object {
        private const val TAG = "NotificationController"
        private const val ACTION_STOP = "STOP"
    }

    private val context = service.applicationContext
    private val actionReceiver = ActionReceiver()

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }
    override fun start() {
        service.startForeground(1, createNotification())
        context.registerReceiver(actionReceiver, IntentFilter().apply {
            addAction(ACTION_STOP)
        })
    }

    override fun stop() {
        context.unregisterReceiver(actionReceiver)
        service.stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            TAG,
            "Running Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(Uri.EMPTY, null)
        }
        notificationManager.createNotificationChannel(channel)
        return Notification.Builder(context, channel.id).apply {
            setSmallIcon(R.drawable.ic_notification)
            setOngoing(true)
            setShowWhen(true)
            setWhen(System.currentTimeMillis())
            setContentTitle("Running")
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(
                        context,
                        R.drawable.ic_notification
                    ),
                    "Stop",
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(ACTION_STOP).setPackage(context.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            )
        }.build()
    }

    private inner class ActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_STOP -> callback.onStopClicked()
            }
        }

    }

    interface Listener {
        fun onStopClicked()
    }
}