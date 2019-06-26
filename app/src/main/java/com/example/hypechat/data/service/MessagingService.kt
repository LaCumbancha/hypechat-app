package com.example.hypechat.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.hypechat.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    private val TAG = "MessagingService"

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        super.onNewToken(token)

        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        //sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: " + remoteMessage?.from)

        remoteMessage?.let { rm ->
            Log.d(TAG, "Message data payload: ${rm.data}")
            Log.d(TAG, "Message Notification Body: " + rm.notification?.body)

            rm.notification?.let {
                sendNotification(it, rm.data)
            }
        }
    }
    //data: MutableMap<String, String>
    private fun sendNotification(notification: RemoteMessage.Notification, playload: Map<String, String>){

        //val title = data["title"]
        val sender = playload["sender"]
        val title = notification.title
        val body = notification.body

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "hypechat"

        @RequiresApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Hypechat Notification", NotificationManager.IMPORTANCE_MAX)

            //config
            notificationChannel.description = "Hypechat Notification Channel"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.WHITE
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 500)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_action_hc)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher_hypechat))
            .setContentTitle(title)
            .setContentText(body)

        notificationManager.notify(1, notificationBuilder.build())
    }
}