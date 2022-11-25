package com.example.aifriend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.example.aifriend.Utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import java.util.*

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("tag", "From: ${message.from}")

        if(message.data.isNotEmpty()) {
            Log.d("tag", "Message: ${message.data}")
            if(true) {
                scheduleJob()
            } else {
                handleNow()
            }
        }

        message.notification?.let {
            it.title?.let { it1 -> it.body?.let { it2 -> sendNotification(it1, it2) } }
            Log.d("tag", "Message Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("tag", "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    // 메세지 페이로드가 있을때 실행됨
    private fun scheduleJob() {
        Log.d("tag", "schedule Job")
    }

    // 10초 이내..
    private fun handleNow() {
        Log.d("tag", "Short lived task is done")

    }

    // 서버에 토큰 유지
    private fun sendRegistrationToServer(token: String?) {
        Log.d("tag", "SENDSENDSEND--------- ")
    }

    private fun sendNotification(title: String, message: String) {


        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notificationID = Random()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID.nextInt(), notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel Description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}