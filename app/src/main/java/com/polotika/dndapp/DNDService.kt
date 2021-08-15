package com.polotika.dndapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.min


class DNDService : Service() {
    lateinit var builder: NotificationCompat.Builder
    private var mNotificationManager: NotificationManager? = null
    private val TAG = "DNDService"
    private val notificationID = 1
    private val notificationChannelID = "notificationChannelID"
    private val notificationChannelName = "notificationChannelName"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        //generateForegroundNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "start" -> {
                makeTheAppSilent()
                val millis = intent.getLongExtra("millis", 60000 * 60 * 8L)
                //generateForegroundNotification()
                testTimer(1, 1)


            }
            "stop" -> {
                makeTheAppNormal()
                stopSelf()
            }
        }


        Log.d(TAG, "onStartCommand: ")
        return START_STICKY
    }

    private fun getMinuteText(t: Long, minutesSize: Long): String {
        val d = minutesSize - t
        val minutes: Int = (d % 60).toInt()
        val hours: Int = ((d / 60) / 10).toInt()
        return "$hours hours and $minutes minutes remaining"
    }

    private fun testTimer(minutesSize: Long, intervalInMinutes: Long) {

        Observable.interval(intervalInMinutes, TimeUnit.MINUTES).take(minutesSize)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    makeTheAppSilent()
                    generateForegroundNotification(0,minutesSize)
                    //updateNotification(getMinuteText(0, minutesSize), true)
                }

                override fun onNext(t: Long) {
                    updateNotification(text = getMinuteText(t+1, minutesSize), true)
                }

                override fun onError(e: Throwable) {
                    makeTheAppNormal()
                    Log.e(TAG, "onError: ${e.localizedMessage}")
                }

                override fun onComplete() {
                    updateNotification(text = "DND is Closed", onGoing = false)
                    makeTheAppNormal()
                    stopService(Intent(this@DNDService,DNDService::class.java))
                    //stopSelf()
                }

            })
    }

    override fun onDestroy() {
        stopService(Intent(this,DNDService::class.java))
        super.onDestroy()
    }


    private fun generateForegroundNotification(t: Long,minutesSize: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            if (mNotificationManager == null) {
                mNotificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(
                        notificationChannelID, notificationChannelName,
                        NotificationManager.IMPORTANCE_MIN
                    )
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }

            builder = NotificationCompat.Builder(this, notificationChannelID)
            builder.setContentTitle("Do not disturb")
                .setTicker("Ticker")
                .setContentText(getMinuteText(t,minutesSize)) //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_sleep)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            builder.color = resources.getColor(R.color.purple_200)


            startForeground(notificationID, builder.build())
        }

    }

    private fun updateNotification(text: String, onGoing: Boolean) {
        builder.setContentText(text)
        builder.setOngoing(onGoing)
        mNotificationManager?.notify(notificationID, builder.build())
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