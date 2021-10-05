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
import android.os.CountDownTimer
import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.function.Predicate


class DNDService : Service() {
    // TODO try Alarm Manager in background to solve timer bug and shutdown issue
    lateinit var builder: NotificationCompat.Builder
    private var mNotificationManager: NotificationManager? = null
    private val TAG = "DNDService"
    private val notificationID = 1
    private val notificationChannelID = "notificationChannelID"
    private val notificationChannelName = "notificationChannelName"
    private var timer: CountDownTimer? = null

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
        Log.d(TAG, "onStartCommand: ${SystemClock.uptimeMillis()}")
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        if (intent!=null){
            when (intent.action) {
                START -> {
                    Log.d(TAG, "start: ${SystemClock.uptimeMillis()}")
                    startDNDMode()
                }
                STOP -> {
                    stopDNDMode()

                }
            }
        }else{
            runBlocking {
                if (PreferencesServices(this@DNDService).getTime.first() != -1L){
                    startDNDMode()
                }else{
                    stopDNDMode()
                }
            }
        }



        return START_STICKY
    }

    private fun generateMinuteText(t: Int, minutesSize: Int): String {
        val d = t - minutesSize

        val minutes: Int = (d % 60)
        val hours: Int = ((d / 60) % 24)
        return if (hours > 0) {
            "$hours hours and $minutes minutes remaining until DND is off"
        } else {
            "$minutes minutes remaining until DND is off"

        }
    }

    private fun generateMinuteText(d: Int): String {

        val minutes: Int = (d % 60)
        val hours: Int = ((d / 60) % 24)
        return if (hours > 0) {
            if(minutes==0){
                "$hours hours remaining until DND is off"
            }else{
                "$hours hours and $minutes minutes remaining until DND is off"
            }
        } else {
            "$minutes minutes remaining until DND is off"

        }
    }

    private fun startTimer(minutesSize: Long, intervalInMinutes: Long) {

        Observable.interval(intervalInMinutes, TimeUnit.MINUTES).take(minutesSize)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    startDNDMode()
                    generateForegroundNotification()
                }

                override fun onNext(t: Long) {
                    updateNotification(
                        text = generateMinuteText(
                            (t + 1).toInt(),
                            minutesSize.toInt()
                        ), true
                    )
                }

                override fun onError(e: Throwable) {
                    stopDNDMode()
                    Log.e(TAG, "onError: ${e.localizedMessage}")
                }

                override fun onComplete() {
                    updateNotification(text = "DND is Closed", onGoing = false)
                    stopDNDMode()
                    stopSelf()
                }

            })

    }

    private fun generateForegroundNotification() {
        Log.d(TAG, "generateForegroundNotification: ${SystemClock.uptimeMillis()}")
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

            val stopAction =
                NotificationCompat.Action(0, getString(R.string.stop_dnd), stopPendingIntent)

            builder = NotificationCompat.Builder(applicationContext, notificationChannelID)
            builder.setContentTitle(getString(R.string.notif_title))
                .setTicker(getString(R.string.notif_ticker))
                .setContentText(
                  "DND Mode is active"
                )
                .setSmallIcon(R.drawable.ic_sleep)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(stopAction)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
            builder.color = getColor(R.color.purple_200)

            Log.d(TAG, "generateForegroundNotification: startForeground")
            startForeground(notificationID, builder.build())
        }

    }

    private fun updateNotification(text: String, onGoing: Boolean) {
        Log.d(TAG, "updateNotification: ${SystemClock.uptimeMillis()}")
        builder.setContentText(text)
        when (onGoing) {
            false -> builder.clearActions()
        }
        mNotificationManager?.notify(notificationID, builder.build())
    }

    private fun startDNDMode() {
        generateForegroundNotification()

        Log.d(TAG, "makeTheAppSilent: Silent")
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)

        var s :Long = -1
        runBlocking {
            CoroutineScope(IO).async{
                val job :Deferred<Long> = async {
                    s = PreferencesServices(this@DNDService).getTime.first()
                    s
                }
                s = job.await()
            }.await()
        }


        s -=Calendar.getInstance().timeInMillis
        timer = object : CountDownTimer(s, 5000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "onTick: $millisUntilFinished")
                val remainingTime: Int = (millisUntilFinished / 1000 / 60).toInt()
                updateNotification(text = generateMinuteText(remainingTime + 1), true)
            }
            override fun onFinish() {
                Log.d(TAG, "onFinish: ")
                updateNotification(text = "DND is Closed", onGoing = false)
                stopDNDMode()
            }
        }.start()
    }

    private fun stopDNDMode() {
        CoroutineScope(IO).launch{
            PreferencesServices(this@DNDService).setTime(-1)
        }
        Log.d(TAG, "makeTheAppNormal: Alarms")
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        CoroutineScope(IO).launch {
            PreferencesServices(this@DNDService).setTime(-1L)
        }
        stopForeground(true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        timer?.cancel()
        stopSelf()
    }


}