package com.polotika.dndapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class DNDService : Service() {
    lateinit var builder: NotificationCompat.Builder
    private var mNotificationManager :NotificationManager?=null
    private val TAG = "DNDService"
    private val notificationID = 1
    private val notificationChannelID = "notificationChannelID"
    private val notificationChannelName = "notificationChannelName"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: ")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "start" ->{
                generateForegroundNotification()}
            "stop" ->{stopSelf()}
        }


        Log.d(TAG, "onStartCommand: ")
        return START_STICKY
    }

    private fun startTimer(millis :Long,interval:Long){
        object : CountDownTimer(millis, interval) {

            override fun onTick(millisUntilFinished: Long) {
               // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                //mTextField.setText("done!")
            }
        }.start()
    }

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(notificationChannelID, notificationChannelName,
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)

                mNotificationManager?.createNotificationChannel(notificationChannel)
            }

            builder = NotificationCompat.Builder(this,notificationChannelID)
            builder.setContentTitle("DND Mode is on")
                .setTicker("Ticker")
                .setContentText("") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            builder.color = resources.getColor(R.color.purple_200)

            startForeground(notificationID, builder.build())
        }

    }


}