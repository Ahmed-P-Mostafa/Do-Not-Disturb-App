package com.polotika.dndapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class DNDService : Service() {
    lateinit var builder: NotificationCompat.Builder
    private var mNotificationManager: NotificationManager? = null
    private val TAG = "DNDService"
    private val notificationID = 1
    private val notificationChannelID = "notificationChannelID"
    private val notificationChannelName = "notificationChannelName"

    // constants
    private val STOP = "STOP"
    private val START = "START"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        //generateForegroundNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        when (intent?.action) {
            START -> {
                makeTheAppSilent()
                val millis = intent.getLongExtra("millis", 60000 * 60 * 8L)
                val minutesSize = millis/1000/60
                startTimer(minutesSize,1)


            }
            STOP -> {
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
        val hours: Int = ((d / 60) % 24).toInt()
       return if (hours>0){
           "$hours hours and $minutes minutes remaining until DND is off"
       }else{
           "$minutes minutes remaining until DND is off"

       }
    }

    private fun startTimer(minutesSize: Long, intervalInMinutes: Long) {

        Observable.interval(intervalInMinutes, TimeUnit.MINUTES).take(minutesSize)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    makeTheAppSilent()
                    generateForegroundNotification(0, minutesSize)
                }

                override fun onNext(t: Long) {
                    updateNotification(text = getMinuteText(t + 1, minutesSize), true)
                }

                override fun onError(e: Throwable) {
                    makeTheAppNormal()
                    Log.e(TAG, "onError: ${e.localizedMessage}")
                }

                override fun onComplete() {
                    updateNotification(text = "DND is Closed", onGoing = false)
                    makeTheAppNormal()
                    stopSelf()
                }

            })
    }


    private fun generateForegroundNotification(t: Long, minutesSize: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(applicationContext, 0, intentMainLanding, 0)
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
            val stopIntent = Intent(applicationContext, DNDService::class.java)
            stopIntent.action = STOP

            val stopPendingIntent = PendingIntent.getService(
                applicationContext,
                0,
                stopIntent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val stopAction = NotificationCompat.Action(0, getString(R.string.stop_dnd), stopPendingIntent)

            builder = NotificationCompat.Builder(applicationContext, notificationChannelID)
            builder.setContentTitle(getString(R.string.notif_title))
                .setTicker(getString(R.string.notif_ticker))
                .setContentText(
                    getMinuteText(
                        t,
                        minutesSize
                    )
                )
                .setSmallIcon(R.drawable.ic_sleep)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(stopAction)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
            builder.color = resources.getColor(R.color.purple_200)

            startForeground(notificationID, builder.build())
        }

    }

    private fun updateNotification(text: String, onGoing: Boolean) {
        builder.setContentText(text)
        when (onGoing) {
            false -> builder.clearActions()
        }
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