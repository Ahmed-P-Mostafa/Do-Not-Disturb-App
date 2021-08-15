package com.polotika.dndapp

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.polotika.dndapp.databinding.ActivityMainBinding
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    private val manager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val viewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.timePicker.hour = getHour()
        binding.timePicker.minute = getMinute()
        binding.startButton.setOnClickListener {
            //binding.startButton.text = "Stop"

            val startIntent = Intent(this, DNDService::class.java)
            startIntent.action = "start"
            val calendar = Calendar.getInstance()
            val calendar2 = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
            calendar.set(Calendar.MINUTE, binding.timePicker.minute)
            val millis = calendar.timeInMillis - calendar2.timeInMillis
            startIntent.putExtra("millis", millis)

            startService(startIntent)
            /*
                val stopIntent = Intent(this,DNDService::class.java)
                stopIntent.action = "stop"
                startService(stopIntent)
            */

        }
    }


    override fun onResume() {
        super.onResume()
        isMyServiceRunning(DNDService::class.java).apply {
            when (this) {
                true -> {
                    binding.startButton.text = getString(R.string.stop)
                }
                false -> {
                    binding.startButton.text = getString(R.string.start)
                }

            }
        }
        if (!manager.isNotificationPolicyAccessGranted) {
            // if user canceled the permission while app is running go to Ask permission again
            startActivity(Intent(this, DNDPermissionActivity::class.java))
            finish()
        }
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun getHour(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun getMinute(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MINUTE)
    }


}