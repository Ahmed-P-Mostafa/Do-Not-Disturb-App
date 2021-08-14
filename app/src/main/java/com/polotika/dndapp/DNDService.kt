package com.polotika.dndapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*


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
        generateForegroundNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            "start" ->{
                makeTheAppSilent()
                val millis = intent.getLongExtra("millis", 60000*60*8L)
                generateForegroundNotification()
                startTimer(millis,1000)

            }
            "stop" ->{
                makeTheAppNormal()
                stopSelf()
            }
        }


        Log.d(TAG, "onStartCommand: ")
        return START_STICKY
    }

    private fun startTimer(millis :Long,interval:Long){
        object : CountDownTimer(millis, interval) {


            override fun onTick(millisUntilFinished: Long) {
                var millis = millisUntilFinished
                val hourInMillis =  1000*60*60L
                val minutesInMillis = 1000*60L
                val secondsInMillis = 1000L

                val hours =   millisUntilFinished / hourInMillis
                millis = hours*hourInMillis - millisUntilFinished

                val minutes = millis % minutesInMillis
                millis = minutes*minutesInMillis - millis
                val seconds = millis/ secondsInMillis
                Log.d(TAG, "onTick: $hourInMillis")
                    updateNotification("$hours hours and $minutes minutes $seconds remaining: " )
            }

            override fun onFinish() {
                builder.setContentText("Done").setOngoing(false)
                mNotificationManager?.notify(notificationID,builder.build())
                stopSelf()
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
            }

            builder = NotificationCompat.Builder(this,notificationChannelID)
            builder.setContentTitle("Do not disturb")
                .setTicker("Ticker")
                .setContentText("DND Mode is on...") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_sleep)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            builder.color = resources.getColor(R.color.purple_200)


            startForeground(notificationID, builder.build())
        }

    }
    private fun updateNotification(text:String){
        builder.setContentText(text)
        mNotificationManager?.notify(notificationID,builder.build())
    }

    private fun makeTheAppSilent() {
        Log.d(TAG, "makeTheAppSilent: Silent")

        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    private fun makeTheAppNormal() {
        Log.d(TAG, "makeTheAppNormal: Alarms")
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }


}