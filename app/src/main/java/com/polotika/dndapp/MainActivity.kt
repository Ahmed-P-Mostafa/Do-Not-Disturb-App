package com.polotika.dndapp

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IInterface
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.polotika.dndapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    private val manager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val viewModel :SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        isMyServiceRunning(DNDService::class.java).apply {
            when(this){
                true->{binding.startButton.text = getString(R.string.stop)}
                false->{binding.startButton.text = getString(R.string.start)}

            }
        }
        binding.timePicker.hour = getHour()
        binding.timePicker.minute = getMinute()
        binding.startButton.setOnClickListener {
            if (isMyServiceRunning(DNDService::class.java)){
                val startIntent = Intent(this,DNDService::class.java)
                startIntent.action = "start"

                startService(startIntent)
            }else{
                val stopIntent = Intent(this,DNDService::class.java)
                stopIntent.action = "stop"
                startService(stopIntent)
            }

        }


    }



    override fun onResume() {
        super.onResume()
        if (!manager.isNotificationPolicyAccessGranted){
            startActivity(Intent(this,DNDPermissionActivity::class.java))
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

    private fun getHour():Int{
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }
    private fun getMinute():Int{
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MINUTE)
    }



    private fun makeTheAppSilent() {
        Log.d(TAG, "makeTheAppSilent: Silent")

        manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    private fun makeTheAppNormal() {
        Log.d(TAG, "makeTheAppNormal: Alarms")
        manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }

}